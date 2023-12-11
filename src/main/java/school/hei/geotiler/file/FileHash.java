package school.hei.geotiler.file;

import school.hei.geotiler.PojaGenerated;

@PojaGenerated
public record FileHash(FileHashAlgorithm algorithm, String value) {}
