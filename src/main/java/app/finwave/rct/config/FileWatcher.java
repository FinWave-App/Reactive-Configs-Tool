package app.finwave.rct.config;

import app.finwave.rct.reactive.InvalidationListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher {
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);

        return thread;
    });

    protected WatchService watchService = FileSystems.getDefault().newWatchService();

    protected HashMap<Watchable, List<WatchInfo>> keyToListeners = new HashMap<>();

    public FileWatcher(long watchPeriod, TimeUnit timeUnit) throws IOException {
        executorService.scheduleAtFixedRate(this::checkAll, 0, watchPeriod, timeUnit);
    }

    public void watch(File file, InvalidationListener listener) throws IOException {
        Path path = file.toPath();
        Path parent = path.getParent();

        if (!keyToListeners.containsKey(parent)) {
            keyToListeners.put(parent, new ArrayList<>());
            parent.register(watchService, ENTRY_MODIFY);
        }

        keyToListeners.get(parent).add(new WatchInfo(path, listener));
    }

    protected void checkAll() {
        try {
            WatchKey key;
            while ((key = watchService.poll()) != null) {
                List<WatchInfo> watchInfos = keyToListeners.get(key.watchable());

                if (watchInfos == null)
                    continue;

                try {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Object context = event.context();

                        if (context instanceof Path) {
                            Path path = (Path) context;

                            Optional<WatchInfo> registered = watchInfos.stream().filter((i) -> {
                                try {
                                    return Files.isSameFile(path, i.filePath);
                                } catch (IOException e) {
                                    return false;
                                }
                            }).findFirst();

                            if (registered.isPresent()) {
                                registered.get().listener.invalidated();
                                continue;
                            }
                        }

                        watchInfos.forEach((i) -> i.listener.invalidated());
                    }
                }finally {
                    key.reset();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class WatchInfo {
    public final Path filePath;
    public final InvalidationListener listener;

    public WatchInfo(Path filePath, InvalidationListener listener) {
        this.filePath = filePath;
        this.listener = listener;
    }
}