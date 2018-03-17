package at.jku.ssw.java.bytecode.reducer;

import at.jku.ssw.java.bytecode.reducer.reducers.Reducer;

import java.io.IOException;
import java.nio.file.Path;

// TODO make runnable?
public class DeltaTest {
    private final String  name;
    private final Reducer reducer;

    public DeltaTest(String name, Reducer reducer) {
        this.name = name;
        this.reducer = reducer;
    }

    public void initTestDir(Path from) throws IOException {
//        Path workingDir = Paths.get(".");
//        Path outDir     = workingDir.resolve(JReduce.TEMP_DIR).resolve(name);
//
//        FileUtils.copyDirectory(from.toFile(), outDir.toFile());
    }

    public void transform() {
//        initTestDir();
    }
}
