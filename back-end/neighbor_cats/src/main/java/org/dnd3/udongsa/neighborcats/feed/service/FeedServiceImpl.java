package org.dnd3.udongsa.neighborcats.feed.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.dnd3.udongsa.neighborcats.cat.dto.CatDto;
import org.dnd3.udongsa.neighborcats.cat.entity.Cat;
import org.dnd3.udongsa.neighborcats.cat.entity.CatMapper;
import org.dnd3.udongsa.neighborcats.cat.service.CatService;
import org.dnd3.udongsa.neighborcats.feed.dto.FeedCommentDto;
import org.dnd3.udongsa.neighborcats.feed.dto.FeedDto;
import org.dnd3.udongsa.neighborcats.feed.dto.FeedModifyDto;
import org.dnd3.udongsa.neighborcats.feed.dto.FeedSaveDto;
import org.dnd3.udongsa.neighborcats.feed.dto.FeedSearchDto;
import org.dnd3.udongsa.neighborcats.feed.dto.PagingDto;
import org.dnd3.udongsa.neighborcats.feed.entity.Feed;
import org.dnd3.udongsa.neighborcats.feed.entity.FeedMapper;
import org.dnd3.udongsa.neighborcats.feed.repository.FeedRepository;
import org.dnd3.udongsa.neighborcats.imgfile.dto.ImgFileDto;
import org.dnd3.udongsa.neighborcats.security.service.SecurityContextService;
import org.dnd3.udongsa.neighborcats.servant.dto.AuthorDto;
import org.dnd3.udongsa.neighborcats.servant.entity.Servant;
import org.dnd3.udongsa.neighborcats.servant.entity.ServantMapper;
import org.dnd3.udongsa.neighborcats.servant.service.ServantService;
import org.dnd3.udongsa.neighborcats.tag.TagDto;
import org.dnd3.udongsa.neighborcats.util.TimeDescService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

  private final FeedRepository repo;
  private final FeedCommentService commentService;
  private final FeedTagService feedTagService;
  private final FeedImgService feedImgService;
  private final SecurityContextService securityService;
  private final FeedLikeService feedLikeService;
  private final TimeDescService timeDescService;
  private final ServantService servantService;
  private final CatService catService;

  @Override
  @Transactional(readOnly = true)
  public PagingDto<FeedDto> findAll(FeedSearchDto searchDto) {
    Pageable pageable = PageRequest.of(searchDto.getPageNumber(), searchDto.getPageSize());
    Page<Feed> pageFeeds = repo.findAll(pageable);
    List<FeedDto> feedDtos = new ArrayList<>();
    for(Feed feed : pageFeeds.getContent()){
      feedDtos.add(toDto(feed));
    } 
    PagingDto<FeedDto> pagingDto = PagingMapper.map(pageFeeds, feedDtos);
    return pagingDto;
  }

  private FeedDto toDto(Feed feed){
    List<TagDto> feedTags = feedTagService.getAllByFeed(feed);
    List<FeedCommentDto> comments = commentService.getAllByFeed(feed);
    List<ImgFileDto> imgDtos = feedImgService.getAllByFeed(feed);
    AuthorDto authorDto = ServantMapper.map(feed.getAuthor());
    Boolean isLike = feedLikeService.isLikeByServant(securityService.getLoggedUserEmail(), feed);
    long numberOfLikes = feedLikeService.getNumberOfLikes(feed);
    int numberOfComments = comments.size();
    LocalDateTime createdDateTime = feed.getCreatedAt();
    String timeDesc = timeDescService.generate(createdDateTime);
    CatDto catDto = CatMapper.map(feed.getCat());
    FeedDto feedDto = FeedMapper.map(feed, feedTags, comments, imgDtos, authorDto, isLike, numberOfLikes, numberOfComments, createdDateTime, timeDesc, catDto);
    return feedDto;
  }

  @Override
  @Transactional
  public FeedDto save(FeedSaveDto saveDto) {
    Servant author = servantService.findServantByEmail(securityService.getLoggedUserEmail());
    Cat cat = catService.findCatById(saveDto.getCatId());
    Feed feed = FeedMapper.map(saveDto, author, cat);
    repo.save(feed);
    feedImgService.save(saveDto.getImgFiles(), feed);
    feedTagService.save(saveDto.getTagIds(), feed);
    return toDto(feed);
  }

  @Override
  @Transactional(readOnly = true)
  public FeedDto findById(Long id) {
    Feed feed = repo.findById(id).orElseThrow();
    return toDto(feed);
  }

  @Override
  @Transactional
  public FeedDto delete(Long id) {
    Feed feed = repo.findById(id).orElseThrow();
    feedTagService.deleteByFeed(feed);
    commentService.deleteByFeed(feed);
    feedImgService.deleteByFeed(feed);
    feedLikeService.deleteByFeed(feed);
    FeedDto feedDto = new FeedDto();
    feedDto.setId(feed.getId());
    repo.delete(feed);
    return feedDto;
  }

  @Override
  @Transactional
  public FeedDto modify(Long id, FeedModifyDto modifyDto) {
    Feed persist = repo.findById(id).orElseThrow();   
    Cat modifyCat = catService.findCatById(modifyDto.getCatId());
    persist.update(modifyDto.getContent(), modifyCat);
    feedImgService.deleteByImgFileIds(persist, modifyDto.getRemoveImgFileIds());
    feedImgService.save(modifyDto.getInsertImgFiles(), persist);
    feedTagService.update(persist, modifyDto.getFeedTagIds());
    return toDto(persist);
  }


}