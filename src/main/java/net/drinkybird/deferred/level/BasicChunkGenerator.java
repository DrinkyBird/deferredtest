package net.drinkybird.deferred.level;

public class BasicChunkGenerator extends ChunkGenerator {
	private Chunk chunk;
	
	@Override
	public void generate(final Chunk chunk) {
		this.chunk = chunk;

	}
}
