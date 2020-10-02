package org.dnd3.udongsa.neighborcats.feed.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.dnd3.udongsa.neighborcats.address.Address;
import org.dnd3.udongsa.neighborcats.exception.CustomException;
import org.dnd3.udongsa.neighborcats.feed.dto.FeedCommentDto;
import org.dnd3.udongsa.neighborcats.feed.dto.FeedDto;
import org.dnd3.udongsa.neighborcats.feed.dto.FeedModifyDto;
import org.dnd3.udongsa.neighborcats.feed.dto.FeedSaveDto;
import org.dnd3.udongsa.neighborcats.feed.dto.FeedSearchDto;
import org.dnd3.udongsa.neighborcats.feed.dto.PagingDto;
import org.dnd3.udongsa.neighborcats.feed.entity.EFilterType;
import org.dnd3.udongsa.neighborcats.feed.entity.ESortType;
import org.dnd3.udongsa.neighborcats.feed.entity.Feed;
import org.dnd3.udongsa.neighborcats.feed.entity.FeedMapper;
import org.dnd3.udongsa.neighborcats.feed.repository.FeedRepository;
import org.dnd3.udongsa.neighborcats.imgfile.dto.ImgFileDto;
import org.dnd3.udongsa.neighborcats.keep.KeepService;
import org.dnd3.udongsa.neighborcats.role.ERole;
import org.dnd3.udongsa.neighborcats.role.Role;
import org.dnd3.udongsa.neighborcats.security.service.SecurityContextService;
import org.dnd3.udongsa.neighborcats.servant.dto.AuthorDto;
import org.dnd3.udongsa.neighborcats.servant.entity.Servant;
import org.dnd3.udongsa.neighborcats.servant.entity.ServantMapper;
import org.dnd3.udongsa.neighborcats.servant.service.ServantService;
import org.dnd3.udongsa.neighborcats.tag.Tag;
import org.dnd3.udongsa.neighborcats.util.TimeDescService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
  private final FeedCatService feedCatService;
  private final KeepService keepService;
  private final FeedMapperService feedMapperService;
  private final ServantMapper servantMapper;

  @Override
  @Transactional(readOnly = true)
  public PagingDto<FeedDto> findAll(FeedSearchDto searchDto) {
    // build Pageable
    validateSearchDto(searchDto);
    Pageable pageable = PageRequest.of(searchDto.getPageNumber(), searchDto.getPageSize(), Direction.DESC, "createdAt");
    Page<Feed> pageFeeds;
    // get feeds
    if(searchDto.getFilterType() == EFilterType.HOMETOWN && Objects.nonNull(searchDto.getTagId())){
      Address address = securityService.getLoggedUser().getAddress();
      Tag tag = feedTagService.findTagByTagId(searchDto.getTagId());
      pageFeeds = repo.findAllByTagAndAddress(tag, address, pageable); 
    }else if(searchDto.getFilterType() == EFilterType.ALL && searchDto.getSortType() == ESortType.POPULAR){
      pageFeeds = repo.findByOrderByLikesAndCountOfComments(pageable);
    }else if(searchDto.getFilterType() == EFilterType.ALL && searchDto.getSortType() == ESortType.LATEST){
      pageFeeds = repo.findAll(pageable);
    }else{
      pageFeeds = repo.findAll(pageable);
    }
    // feedList -> feedDtoList
    List<FeedDto> feedDtoList = feedMapperService.toDto(pageFeeds.getContent(), false);
    // feedDtoList -> feedDtoList with page
    return PagingMapper.map(pageFeeds, feedDtoList);
  }

  private void validateSearchDto(FeedSearchDto searchDto) {
    // Null 체크
    Integer pageSize = searchDto.getPageSize();
    Integer pageNumber = searchDto.getPageNumber();
    if(Objects.isNull(pageSize) || Objects.isNull(pageNumber)){
      throw new CustomException(HttpStatus.BAD_REQUEST, "pageSize 또는 PageNumber Null입니다.");
    }
    // 숫자 최소값 체크
    if(pageSize <= 0){
      throw new CustomException(HttpStatus.BAD_REQUEST, "PageSize는 1 이상이어야 합니다.");
    }
  }

  @Override
  @Transactional
  public FeedDto save(FeedSaveDto saveDto) {
    Servant author = securityService.getLoggedUser();
    Feed feed = FeedMapper.map(saveDto, author);
    if(Objects.isNull(saveDto.getImgFiles()) || saveDto.getImgFiles().size() == 0){
      throw new CustomException(HttpStatus.BAD_REQUEST, "이미지를 한 개 이상 업로드해야 합니다.");
    }
    repo.save(feed);
    feedImgService.save(saveDto.getImgFiles(), feed);
    if(Objects.nonNull(saveDto.getTagId())){
      feedTagService.save(saveDto.getTagId(), feed);
    }
    if(Objects.nonNull(saveDto.getCatIds()) && saveDto.getCatIds().size() > 0){
      validateCat(saveDto.getCatIds());
      feedCatService.save(saveDto.getCatIds(), feed);
    }
    return toDto(feed, true);
  }

  private void validateCat(List<Long> catIds) {
    String email = securityService.getLoggedUserEmail();
    Servant servant = servantService.findServantByEmail(email);
    boolean doesHave = feedCatService.doesHaveCat(servant, catIds);
    if(!doesHave){
      throw new CustomException(HttpStatus.BAD_REQUEST, "해당 고양이를 가지고 있지 않습니다.", "cats: {}, servantEmail: {}", catIds, email);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public FeedDto findById(Long id) {
    Feed feed = repo.findById(id).orElseThrow();
    return toDto(feed, true);
  }

  private FeedDto toDto(Feed feed, boolean withComments){
    List<ImgFileDto> imgDtos = feedImgService.getAllByFeed(feed);
    AuthorDto authorDto = servantMapper.mapForAuthor(feed.getAuthor());
    Boolean isLike = feedLikeService.isLikeByServant(securityService.getLoggedUser(), feed);
    long numberOfLikes = feed.getLikes().size();
    int numberOfComments = feed.getComments().size();
    LocalDateTime createdDateTime = feed.getCreatedAt();
    String timeDesc = timeDescService.generate(createdDateTime);
    List<FeedCommentDto> comments = new ArrayList<>();
    if(withComments){
      comments = commentService.getAllByFeed(feed);
    }
    return FeedMapper.map(feed, imgDtos, authorDto, isLike, numberOfLikes, numberOfComments, createdDateTime, timeDesc, comments);
  }

  @Override
  @Transactional
  public FeedDto delete(Long id) {
    Feed feed = repo.findById(id).orElseThrow();
    validateAuthor(feed.getAuthor());
    feedTagService.deleteByFeed(feed);
    commentService.deleteByFeed(feed);
    feedImgService.deleteByFeed(feed);
    feedLikeService.deleteByFeed(feed);
    feedCatService.deleteByFeed(feed);
    keepService.deleteByFeed(feed);
    FeedDto feedDto = new FeedDto();
    feedDto.setId(feed.getId());
    repo.delete(feed);
    return feedDto;
  }

  private void validateAuthor(Servant author) {
    String email = securityService.getLoggedUserEmail();
    Servant servant = servantService.findServantByEmail(email);
    if(servant.getId().equals(author.getId())) return;
    for(Role role : servant.getRoles()){
      if(role.getName() == ERole.ROLE_ADMIN) return;
    }
    throw new CustomException(HttpStatus.FORBIDDEN, "작성/수정 권한이 없습니다.", "작성자 이메일: {}, 요청자 이메일: {}", author.getEmail(), servant.getEmail());      
  }

  @Override
  @Transactional
  public FeedDto modify(Long id, FeedModifyDto modifyDto) {
    Feed persist = repo.findById(id).orElseThrow();   
    validateAuthor(persist.getAuthor());
    persist.update(modifyDto.getContent());
    List<Long> removedImgIds = modifyDto.getRemoveImgFileIds();
    List<MultipartFile> insertImgFiles = modifyDto.getInsertImgFiles();
    if(Objects.nonNull(removedImgIds) && removedImgIds.size() > 0){
      feedImgService.deleteByImgFileIds(persist, modifyDto.getRemoveImgFileIds());
    }
    if(Objects.nonNull(insertImgFiles) && insertImgFiles.size() > 0){
      feedImgService.save(modifyDto.getInsertImgFiles(), persist);
    }
    if(Objects.nonNull(modifyDto.getTagId())){
      feedTagService.update(persist, modifyDto.getTagId());
    }
    if(Objects.nonNull(modifyDto.getCatIds())){
      validateCat(modifyDto.getCatIds());
      feedCatService.update(modifyDto.getCatIds(), persist);
    }
    List<ImgFileDto> imgFileDtos = feedImgService.getAllByFeed(persist);
    if(imgFileDtos.size() == 0){
      throw new CustomException(HttpStatus.BAD_REQUEST, "이미지를 한 개 이상 업로드해야 합니다.");
    }
    return toDto(persist, true);
  }


}