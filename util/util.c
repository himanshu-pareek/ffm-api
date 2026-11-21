#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "util.h"

int add(int a, int b) {
  printf("adding %d and %d\n", a, b);
  return a + b;
}

Point add_points(Point p1, Point p2) {
  Point result = {};
  result.x = p1.x + p2.x;
  result.y = p1.y + p2.y;
  result.z = p1.z + p2.z;
  return result;
}

Point normalize_point(Point p) {
  double s_squared = p.x * p.x + p.y * p.y + p.z * p.z;
  if (s_squared >= 1e-6) {
    double s = sqrt(s_squared);
    p.x /= s;
    p.y /= s;
    p.z /= s;
  }
  return p;
}

Point* normalize_points(Point* points, size_t len) {
  Point* result = malloc(len * sizeof(Point));
  for (int i = 0; i < len; ++i) {
    result[i] = normalize_point(points[i]);
  }
  return result;
}
