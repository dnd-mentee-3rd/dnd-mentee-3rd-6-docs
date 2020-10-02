package org.dnd3.udongsa.neighborcats.feed.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.dnd3.udongsa.neighborcats.feed.entity.EFilterType;
import org.dnd3.udongsa.neighborcats.feed.entity.ESortType;

import lombok.Data;

@Data
public class FeedSearchDto {

  private Long tagId;
  private EFilterType filterType;
  private ESortType sortType;
  private Long catId;
  @NotNull
  private Integer pageNumber;
  @Size(min=1)
  private Integer pageSize;
  
}