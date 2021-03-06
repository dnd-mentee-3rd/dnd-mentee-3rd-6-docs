import React, { useCallback, useState } from 'react';
import PropTypes from 'prop-types';
import styled, { css } from 'styled-components';
import Slider from 'react-slick';
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';

import { pallete } from '../../lib/style/pallete';

export const CardImage = styled.div`
  position: relative;
  width: 100vw;
  height: ${({ value }) => `${value}px`};

  display: flex;

  flex-direction: column;
  justify-content: center;
  align-items: center;
  justify-content: center;

  margin-top: 12px;

  overflow: hidden;

  .slick-list {
    width: 100vw;
    height: ${({ value }) => `${value}px`};
  }

  .slick-slide {
    display: flex;
    justify-content: center;
    align-items: center;

    overflow: hidden;

    width: 100vw;
    height: ${({ value }) => `${value}px`};
  }

  .feed-card__img-index {
    position: absolute;
    top: 18px;
    right: 22px;

    display: flex;
    justify-content: center;
    align-items: center;

    height: 25px;
    width: 38px;
    background: rgba(31, 31, 31, 0.7);
    border-radius: 14px;

    font-style: normal;
    font-weight: 500;
    font-size: 11px;
    line-height: 15px;

    color: ${pallete.primary[3]};

    z-index: 10;
  }

  & .img-box {
    width: 100vw;
    height: ${({ value }) => `${value}px`};

    .feed-img {
      width: auto;
      height: 100%;
    }
  }
`;

const LoadImage = styled.div`
  @keyframes loadImg {
    0% {
      background-position: left;
    }
    50% {
      background-position: right;
    }
    100% {
      background-position: left;
    }
  }
  position: absolute;
  top: 0;
  right: 0;
  width: 100%;
  height: ${({ value }) => `${value}px`};

  ${({ checked, checkLoadId }) => {
    return checked !== checkLoadId
      ? css`
          background: linear-gradient(90deg, #f0f0f0, #fafafa, #fff);
          background-size: 400% 400%;
          display: block;

          z-index: 999;

          animation: loadImg 3s ease-out infinite;
        `
      : css`
          display: none;
        `;
  }}
`;

const CardImageWrapprer = ({ feed }) => {
  const [currentSlide, setCurrentSlide] = useState(0);
  const [checkLoadId, setCheckLoadId] = useState(null);
  const heightValue = window.innerWidth;

  const onLoadImage = useCallback(
    (index) => () => {
      setCheckLoadId(() => index);
    },
    [],
  );

  return (
    <CardImage value={heightValue}>
      <span className="feed-card__img-index">
        {currentSlide + 1}/{feed.images.length}
      </span>
      <LoadImage value={heightValue} checked={feed.id} checkLoadId={checkLoadId} />
      <Slider
        dots={false}
        infinite={false}
        speed={500}
        slidesToShow={1}
        slidesToScroll={1}
        arrows={false}
        beforeChange={(current, next) => setCurrentSlide(next)}
      >
        {feed.images.map((image) => (
          <div key={image.id} className="img-box">
            <img
              className="feed-img"
              src={`${
                process.env.NODE_ENV === 'development' ? process.env.REACT_APP_BASE_URL : ''
              }${image.url}`}
              alt={image}
              onLoad={onLoadImage(feed.id)}
            />
          </div>
        ))}
      </Slider>
    </CardImage>
  );
};

CardImageWrapprer.prototype = {
  feed: PropTypes.shape({
    id: PropTypes.number,
    images: PropTypes.arrayOf(PropTypes.object),
  }).isRequired,
  onLoadImage: PropTypes.func.isRequired,
  checkLoadId: PropTypes.bool.isRequired,
};

export default React.memo(CardImageWrapprer);
