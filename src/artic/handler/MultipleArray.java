package artic.handler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


/**
 * Generic multidimensional array wrapper.
 *
 * @param <T> stored element type
 */
public class MultipleArray <T>{
	public static long time = 0;
	
	/**
	 * Number of dimensions.
	 */
	private int dimension;
	
	/**
	 * Backing array.
	 */
	private Object array;
	

	/**
	 * Creates an array and fills it with an initial value.
	 * @param lengths length of each dimension
	 * @param init initial value
	 */
	public MultipleArray(T init,int... lengths) {
		super();
		this.dimension = lengths.length;
		array = Array.newInstance(init.getClass(), lengths);
		initArray(init,array,0); // Recursively fills the array.
	}
	
	public MultipleArray(Class<T> type,int... lengths) {
		super();
		this.dimension = lengths.length;
		array = Array.newInstance(type, lengths);
	}
	
	/**
	 * Finds the leaf array that contains the target element.
	 * @param indexes index array
	 * @return leaf array for element access
	 */
	public T[] findItem(int[] paramIndex, int[] x,int... indexes) {
		Object object = array;
		for (int i = 0; i < indexes.length-1; i++) {
			int index = indexes[i];
			if(x[index] == -1) {
				return null;
			}
			object = ((Object[])object)[paramIndex[index]+ x[index]];
		}
		return (T[])object;
	}
	
	private T[][] findItem(int[] x,int... indexes) {
		Object object = array;
		for (int i = 0; i < indexes.length-1; i++) {
			int index = indexes[i];
			object = ((Object[][])object)[index][x[index]];
		}
		return (T[][])object;
	}
	
	/**
	 * Recursively initializes the backing array.
	 * @param init initial value
	 * @param array array at the current dimension
	 * @param depth recursion depth
	 */
	public void initArray(T init,Object array,int depth) {
		if(depth == dimension - 1) {
			Arrays.fill((T[]) array, init);
			return;
		}
		int length = Array.getLength(array); // Length of the current dimension.
		for(int i = 0 ; i < length; i ++) { // Recurse into the next dimension.
			initArray(init, Array.get(array, i), depth + 1);
		}
	}
	
	
	/**
	 * Returns the element at the given indexes.
	 * @param indexes indexes of the target element
	 * @return element at the indexes
	 */
	public T getObject(int[] paramIndex,int[] x,int[] indexes) {
		/* Recursively reaches the leaf array, then uses the last index to read the element. */
		
//		switch (indexes.length) {
//		case 4:
//			long s = System.nanoTime();
////			System.out.println(Arrays.toString(indexes));
////			System.out.println(Arrays.toString(x));
//			int index000 = paramIndex[indexes[0]][x[indexes[0]]];
//			int index111 = paramIndex[indexes[1]][x[indexes[1]]];
//			int index222 = paramIndex[indexes[2]][x[indexes[2]]];
//			int index333 = paramIndex[indexes[3]][x[indexes[3]]];
//			time += System.nanoTime() - s;
//			return ((T[][][][])array)[index000][index111][index222][index333];
//		}
//		return null;
		T[] item = findItem(paramIndex,x,indexes);
		if(item == null) {
			return null;
		}
		int end = indexes[indexes.length-1];
		int index = paramIndex[end] + x[end];
		return item[index];
	}


	public T getObjectByPC(int[] paramIndex,int[] fpc,int[] lpc) {
		/* Recursively reaches the leaf array, then uses the last index to read the element. */
		Object object = array;
		int i = 0;
		int index = 0;
		for (; i < fpc.length; i++) {
			index = paramIndex[fpc[i]] + lpc[i];
			object = ((Object[])object)[index];
		}
		return (T) object;
	}

//	public T getValueAndIncrease(int[] fpc,int[] x) {
//		Object object = array;
//		for (int i = 0; i < fpc.length-1; i++) {
//			object = ((Object[])object)[x[fpc[i]]];
//		}
//		return (T) object;
//	}

	public int getValue(int[] fpc,int[] x) {
		Object object = array;
		for (int i = 0; i < fpc.length-1 ; i++) {
			object = ((Object[])object)[x[fpc[i]]];
		}
		try {
			return ((int[]) object)[x[fpc[fpc.length-1]]];
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int getValue(int[] vc) {
		Object object = array;
		for (int i = 0; i < vc.length-1 ; i++) {
			object = ((Object[])object)[vc[i]];
		}
		return ((int[]) object)[vc[vc.length-1]];
	}

	public int getValueIncrease(int[] fpc,int[] x) {
		Object object = array;
		for (int i = 0; i < fpc.length-1; i++) {
			object = ((Object[])object)[x[fpc[i]]];
		}
		int[] array = (int[])object;
		int val = array[x[fpc[fpc.length-1]]] ++;
		return val;
	}

	public int getValueDescend(int[] fpc,int[] x) {
		Object object = array;
		for (int i = 0; i < fpc.length-1; i++) {
			object = ((Object[])object)[x[fpc[i]]];
		}
		int[] array = (int[])object;
		int val = array[x[fpc[fpc.length-1]]] --;
		return val;
	}



	
	/**
	 * Sets the element at the given indexes.
	 * @param indexes indexes of the target element
	 * @param value value to set
	 */
	public void setObject(int[] paramIndex,int[] x,T value,int... indexes) {
		T[] item = findItem(paramIndex,x,indexes);
		int end = indexes[indexes.length-1];
		item[paramIndex[end]+x[end]] = value;
	}
	
	public Integer getAndIncrease(int[] paramIndex, int[] x, int... indexes) throws Exception {
		T[] item = findItem(paramIndex,x,indexes);
		int end = indexes[indexes.length-1];
		int index = paramIndex[end] +x[end];
		Object value = item[index]; 
		if(value instanceof Integer) {
			value = ((Integer)value) + 1;
			item[index] = (T) value;
			return ((Integer) value);
		}
		else
			throw new Exception();
	}

	public Integer getAndDescend(int[] paramIndex, int[] x, int... indexes) throws Exception {
		T[] item = findItem(paramIndex,x,indexes);
		int end = indexes[indexes.length-1];
		int index = paramIndex[end] +x[end];
		Object value = item[index];
		if(value instanceof Integer) {
			value = ((Integer)value) - 1;
			item[index] = (T) value;
			return ((Integer) value);
		}
		else
			throw new Exception();
	}
	
	public Integer getAndIncrease(int[] x, int... indexes) throws Exception {
		T[][] item = findItem(x,indexes);
		int end = indexes[indexes.length-1];
		Object value = item[end][x[end]]; 
		if(value instanceof Integer) {
			value = ((Integer)value) + 1;
			item[end][x[end]] = (T) value;
			return ((Integer) value);
		}
		else
			throw new Exception();
	}

	public Object getArray() {
		return array;
	}

	public void setArray(Object array) {
		this.array = array;
	}
	
//	public static void main(String[] args) throws Exception {
//		
//	}
	
}
