#ifndef UTIL_H
#define UTIL_H

int add(int a, int b);

typedef struct {
  double x, y, z;
} Point;

Point add_points (Point p1, Point p2);

Point* normalize_points (Point *, size_t len);

#endif
