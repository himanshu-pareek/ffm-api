import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

import java.util.List;

public class FFMDemo {
    static {
	System.loadLibrary("util");
    }
    
    public static void main(String[] args) throws Throwable {
	add();
	addPoints();
	normalizePoints();
    }

    private static void normalizePoints() throws Throwable {
	List<Point> points = List.of(
				     new Point(1.23, 2.3325, 102.287),
				     new Point(344.222, 769.998, 987.32));

	MemoryLayout layout_arr_points = MemoryLayout.sequenceLayout(2, POINT_LAYOUT);
 	MemoryLayout layout_addr_arr_points = ValueLayout.ADDRESS.withTargetLayout(layout_arr_points);
	
	Linker linker = Linker.nativeLinker();
	MemoryLayout sizeT = linker.canonicalLayouts().get("size_t");
	FunctionDescriptor descriptor = FunctionDescriptor.of(layout_addr_arr_points, layout_addr_arr_points, sizeT);
	MemorySegment normalizePointsSegment = SymbolLookup.loaderLookup().findOrThrow("normalize_points");
	MethodHandle normalizeHandle = linker.downcallHandle(normalizePointsSegment, descriptor);
	System.out.println(normalizeHandle);

	try (Arena arena = Arena.ofConfined()) {
	    MemorySegment segment = toSegment(points, arena);
	    System.out.println("Segment size: " + segment.byteSize());
	    var rawResult = (MemorySegment) normalizeHandle.invokeExact(segment, (long) points.size());
	    System.out.println("Raw result: " + rawResult);

	    if (rawResult.equals(MemorySegment.NULL)) {
		System.out.println("Got null");
	    } else {
		long byteSize = points.size() * POINT_LAYOUT.byteSize();
		MemorySegment resultArray = rawResult.reinterpret(byteSize, arena, seg -> {});
		List<Point> normalizedPoints = toPointList(resultArray);
		System.out.println(points);
		System.out.println(normalizedPoints);
	    }
	}
    }
	

    private static void addPoints() throws Throwable {
	Point p = new Point(1.23, 2.3325, 102.287);
	Point q = new Point(344.222, 769.998, 987.32);
	Linker linker = Linker.nativeLinker();
	FunctionDescriptor descriptor = FunctionDescriptor.of(POINT_LAYOUT, POINT_LAYOUT, POINT_LAYOUT);
	MemorySegment addPointsSegment = SymbolLookup.loaderLookup().findOrThrow("add_points");
	MethodHandle addHandle = linker.downcallHandle(addPointsSegment, descriptor);

	try (Arena arena = Arena.ofConfined()) {
	    MemorySegment s1 = toSegment(p, arena);
	    MemorySegment s2 = toSegment(q, arena);

	    MemorySegment result = (MemorySegment) addHandle.invokeExact((SegmentAllocator) arena, s1, s2);
	    Point sum = fromSegment(result);
	    System.out.println("Sum: " + sum);
	}
    }

    private static List<Point> toPointList(MemorySegment segment) {
	return segment.elements(POINT_LAYOUT)
	    .map(s -> fromSegment(s))
	    .toList();
    }

    private static MemorySegment toSegment(List<Point> points, SegmentAllocator allocator) {
	MemorySegment segment = allocator.allocate(POINT_LAYOUT, points.size());
	for (int i = 0; i < points.size(); i++) {
	    Point p = points.get(i);
	    X_ELEMENT_HANDLE.set(segment, 0L, i, p.x());
	    Y_ELEMENT_HANDLE.set(segment, 0L, i, p.y());
	    Z_ELEMENT_HANDLE.set(segment, 0L, i, p.z());
	}
	return segment;
    }

    private static MemorySegment toSegment(Point p, SegmentAllocator allocator) {
	MemorySegment segment = allocator.allocate(POINT_LAYOUT);
	X_HANDLE.set(segment, 0L, p.x());
	Y_HANDLE.set(segment, 0L, p.y());
	Z_HANDLE.set(segment, 0L, p.z());
	return segment;
    }

    private static Point fromSegment(MemorySegment segment) {
	return new Point(
			 (double) X_HANDLE.get(segment, 0L),
			 (double) Y_HANDLE.get(segment, 0L),
			 (double) Z_HANDLE.get(segment, 0L));
    }

    private static void add() throws Throwable {
	Linker linker = Linker.nativeLinker();
	FunctionDescriptor descriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT,
							      ValueLayout.JAVA_INT,
							      ValueLayout.JAVA_INT);
	MemorySegment addSegment = SymbolLookup.loaderLookup().findOrThrow("add");
	MethodHandle addHandle = linker.downcallHandle(addSegment, descriptor);
	int sum = (int) addHandle.invokeExact(123, 456);
	System.out.printf("Sum: %d\n", sum);
    }

    private static MemoryLayout POINT_LAYOUT =
	MemoryLayout.structLayout(ValueLayout.JAVA_DOUBLE.withName("x"),
				  ValueLayout.JAVA_DOUBLE.withName("y"),
				  ValueLayout.JAVA_DOUBLE.withName("z"));
    private static VarHandle X_HANDLE =
	POINT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("x"));
    private static VarHandle Y_HANDLE =
	POINT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("y"));
    private static VarHandle Z_HANDLE =
	POINT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("z"));
    private static VarHandle X_ELEMENT_HANDLE =
	POINT_LAYOUT.arrayElementVarHandle(MemoryLayout.PathElement.groupElement("x"));
    private static VarHandle Y_ELEMENT_HANDLE =
	POINT_LAYOUT.arrayElementVarHandle(MemoryLayout.PathElement.groupElement("y"));
    private static VarHandle Z_ELEMENT_HANDLE =
	POINT_LAYOUT.arrayElementVarHandle(MemoryLayout.PathElement.groupElement("z"));
}

record Point(double x, double y, double z) {}
