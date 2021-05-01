package net.drinkybird.deferred.level;

public class ChunkID {
	private ChunkID() { }
	
	public static long encode(int x, short y, int z) {
		if (x < -8388608 || x > 8388607) {
			throw new IllegalArgumentException("x outside the range of a 24-bit number (-8388608 < " + x + " < 8388607)");
		}
		
		if (z < -8388608 || z > 8388607) {
			throw new IllegalArgumentException("z outside the range of a 24-bit number (-8388608 < " + z + " < 8388607)");
		}
		
		long id = 0L;
	    id |= (z & 0x0000000000FFFFFFL) << 40;
	    id |= (x & 0x0000000000FFFFFFL) << 16;
	    id |= (y & 0x000000000000FFFFL);
	    return id;
	}
	
	public static int decodeX(long id) {
		long x = ((id >> 16) & 0x00FFFFFFL);
	    return (int)((x & 0x00800000L) != 0 ? (x | ~0x00FFFFFFL) : x);
	}
	
	public static short decodeY(long id) {
	    long y = id & 0x0000FFFFL;
	    return (short)((y & 0x00008000L) != 0 ? (y | ~0x0000FFFFL) : y);
	}
	
	public static int decodeZ(long id) {
		long z = ((id >> 40) & 0x00FFFFFFL);
	    return (int)((z & 0x00800000L) != 0 ? (z | ~0x00FFFFFFL) : z);
	}
	
	public static int tileX(long id) {
		return decodeX(id) << Chunk.CHUNK_SHIFT;
	}
	
	public static int tileY(long id) {
		return decodeY(id) << Chunk.CHUNK_SHIFT;
	}
	
	public static int tileZ(long id) {
		return decodeZ(id) << Chunk.CHUNK_SHIFT;
	}
}
