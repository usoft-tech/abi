import TweenOne from "rc-tween-one";
import React, { MouseEventHandler } from "react";
import styled from "styled-components";

class GridLayout {
  gridX: number;
  gridY: number;
  cellWidth: number;
  cellHeight: number;
  grid: any[][];
  constructor(rect: number, width: number, height: number) {
    this.gridX = Math.floor(width / rect);
    this.gridY = Math.floor(height / rect);
    this.cellWidth = width / this.gridX;
    this.cellHeight = height / this.gridY;
    this.grid = [];
    for (let i = 0; i < this.gridY; i += 1) {
      this.grid[i] = [];
      for (let s = 0; s < this.gridX; s += 1) {
        this.grid[i][s] = [];
      }
    }
  }

  getCells = (e: { x: number; y: number; radius: number }) => {
    const gridArray = [];
    const w1 = Math.floor((e.x - e.radius) / this.cellWidth);
    const w2 = Math.ceil((e.x + e.radius) / this.cellWidth);
    const h1 = Math.floor((e.y - e.radius) / this.cellHeight);
    const h2 = Math.ceil((e.y + e.radius) / this.cellHeight);
    for (let c = h1; c < h2; c += 1) {
      for (let l = w1; l < w2; l += 1) {
        gridArray.push(this.grid[c][l]);
      }
    }
    return gridArray;
  };

  hasCollisions = (t: { x: number; y: number; radius: number }) =>
    this.getCells(t).some((e: any) => e.some((v: any) => this.collides(t, v)));

  collides = (
    t: { x: number; y: number; radius: number },
    a: { x: number; y: number; radius: number },
  ) => {
    if (t === a) {
      return false;
    }
    const n = t.x - a.x;
    const i = t.y - a.y;
    const r = t.radius + a.radius;
    return n * n + i * i < r * r;
  };

  add = (value: { x: number; y: number; radius: number }) => {
    this.getCells(value).forEach((item: any) => {
      item.push(value);
    });
  };
}

const getPointPos = (width: number, height: number, length: number) => {
  const grid = new GridLayout(150, width, height);
  const posArray = [];
  const num = 500;
  const radiusArray = [20, 35, 60];
  for (let i = 0; i < length; i += 1) {
    let radius;
    let pos;
    for (let j = 0; j < num; j += 1) {
      radius = radiusArray[Math.floor(Math.random() * radiusArray.length)];
      pos = {
        x: Math.random() * (width - radius * 2) + radius,
        y: Math.random() * (height - radius * 2) + radius,
        radius,
      };
      if (!grid.hasCollisions(pos)) {
        break;
      }
    }
    posArray.push(pos);
    grid.add(pos!);
  }
  return posArray;
};

const getDistance = (
  t: { x: number; y: number },
  a: { x: number; y: number },
) => Math.sqrt((t.x - a.x) * (t.x - a.x) + (t.y - a.y) * (t.y - a.y));

class Point extends React.PureComponent<any> {
  render() {
    const { tx, ty, x, y, opacity, backgroundColor, radius, ...props } =
      this.props;
    let transform;
    let zIndex = 0;
    let animation: any = {
      y: (Math.random() * 2 - 1) * 20 || 15,
      duration: 3000,
      delay: Math.random() * 1000,
      yoyo: true,
      repeat: -1,
    };
    if (tx && ty) {
      if (tx !== x && ty !== y) {
        const distance = getDistance({ x, y }, { x: tx, y: ty });
        const g = Math.sqrt(2000000 / (0.1 * distance * distance));
        transform = `translate(${(g * (x - tx)) / distance}px, ${(g * (y - ty)) / distance}px)`;
      } else if (tx === x && ty === y) {
        transform = `scale(${80 / radius})`;
        animation = { y: 0, yoyo: false, repeat: 0, duration: 300 };
        zIndex = 1;
      }
    }
    return (
      <div
        style={{
          left: x - radius,
          top: y - radius,
          width: radius * 1.8,
          height: radius * 1.8,
          opacity,
          zIndex,
          transform,
        }}
        {...props}
      >
        <TweenOne
          animation={animation}
          style={{
            backgroundColor,
          }}
          className={`${this.props.className}-child`}
        />
      </div>
    );
  }
}

const Wrapper = styled.div`
  overflow: hidden;
  height: 100vh;
  width: 100vw;
  position: fixed;
  top: 0;
  left: 0;
  background: #019bf0;

  .linked-animate-box {
    position: absolute;
    width: 1920px;
    height: 800px;
    display: block;
    left: -100%;
    top: 0;
    bottom: 0;
    right: -100%;
    margin: auto;
  }

  .linked-animate-block {
    position: absolute;
    transition: transform 0.45s ease;
  }

  .linked-animate-block-child {
    border-radius: 100%;
    width: 100%;
    height: 100%;
  }
`;

class LinkedAnimate extends React.Component<any, any> {
  static defaultProps = {
    className: "linked-animate",
  };
  box: any = null;

  num = 50; // 点的个数

  constructor(props: any) {
    super(props);
    this.state = {
      data: getPointPos(1920, 800, this.num).map((item) => ({
        ...item,
        opacity: Math.random() * 0.2 + 0.05,
        backgroundColor: `rgb(${Math.round(Math.random() * 95 + 160)},255,255)`,
      })),
      tx: 0,
      ty: 0,
    };
  }

  onMouseMove: MouseEventHandler = (e) => {
    const cX = e.clientX;
    const cY = e.clientY;
    const boxRect = this.box.getBoundingClientRect();
    const pos = this.state.data
      .map((item: any) => {
        const { x, y, radius } = item;
        return {
          x,
          y,
          distance:
            getDistance({ x: cX - boxRect.x, y: cY - boxRect.y }, { x, y }) -
            radius,
        };
      })
      .reduce(
        (
          a: { x: number; y: number; distance: number },
          b: { x: number; y: number; distance: number },
        ) => {
          if (!a.distance || a.distance > b.distance) {
            return b;
          }
          return a;
        },
      );
    if (pos.distance < 60) {
      this.setState({
        tx: pos.x,
        ty: pos.y,
      });
    } else {
      this.onMouseLeave();
    }
  };

  onMouseLeave = () => {
    this.setState({
      tx: 0,
      ty: 0,
    });
  };

  render() {
    const { className } = this.props;
    const { data, tx, ty } = this.state;
    return (
      <Wrapper className={`${className}-wrapper`}>
        <div
          className={`${className}-box`}
          ref={(c) => {
            this.box = c;
          }}
          onMouseOver={this.onMouseMove}
          onMouseLeave={this.onMouseLeave}
        >
          {data.map((item: any, i: number) => (
            <Point
              {...item}
              tx={tx}
              ty={ty}
              key={i.toString()}
              className={`${className}-block`}
            />
          ))}
        </div>
      </Wrapper>
    );
  }
}

export default LinkedAnimate;
