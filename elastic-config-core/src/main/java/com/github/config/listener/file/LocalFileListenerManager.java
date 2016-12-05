package com.github.config.listener.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import com.github.config.bus.ElasticConfigEvent;
import com.github.config.bus.ElasticConfigEventBus;
import com.github.config.exception.ElasticConfigException;
import com.github.config.group.ZookeeperConfigProfile;
import com.github.config.listener.AbstractListenerManager;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * 本地文件监听管理
 * 
 * @author ZhangWei
 */
@Slf4j
@RequiredArgsConstructor
public class LocalFileListenerManager extends AbstractListenerManager {

    @Getter
    private final ZookeeperConfigProfile configProfile;

    private static ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors
        .newCachedThreadPool());

    @Override
    public void start() {
        addFileListener();
    }

    public class DirectoryListener implements Runnable {

        private final Path path;

        private final WatchService watchService;

        @SneakyThrows
        public DirectoryListener(Path path) {
            this.path = path;
            this.watchService = FileSystems.getDefault().newWatchService();
        }

        @Override
        public void run() {
            registerWatchService();
            rotateListnerEvent();
            closeWatchService();
        }

        /**
         * 注册监控服务
         */
        private void registerWatchService() {
            try {
                path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            }
            catch (IOException e) {
                throw new ElasticConfigException("path {} register monitor event error!", path.toFile()
                    .getAbsolutePath(), e);
            }
        }

        /**
         * 轮巡监听事件
         */
        private void rotateListnerEvent() {
            while (!Thread.interrupted()) {
                if (!listenEvent()) {
                    break;
                }
            }
        }

        /**
         * 关闭监控服务
         */
        private void closeWatchService() {

            try {
                watchService.close();
            }
            catch (IOException e) {
                throw new RuntimeException("close watchService error!", e);
            }
        }

        /**
         * 是否监听事件
         * 
         * @return 是否监听
         */
        private boolean listenEvent() {
            WatchKey signal;
            try {
                Thread.sleep(500L);
                signal = watchService.take();
            }
            catch (InterruptedException e) {
                return false;
            }

            for (WatchEvent<?> event : signal.pollEvents()) {
                log.info("event：" + event.kind() + "," + "filename：" + event.context());
                pushEvent(event);
            }

            return signal.reset();
        }

    }

    /**
     * 发布事件
     * 
     * @param event 文件变更事件
     */
    private void pushEvent(final WatchEvent<?> event) {

        if (isNotified(event)) {
            ElasticConfigEventBus.pushEvent(ElasticConfigEvent.builder()
                .path(((Path) event.context()).toAbsolutePath().toString()).value(event.context().toString())
                .eventType(eventMap.get(event.kind())).build());
        }
    }

    /*
     * 是否发送消息通知
     * @param event 文件变更事件
     */
    private boolean isNotified(WatchEvent<?> event) {
        Path filename = (Path) event.context();
        return eventMap.containsKey(event.kind())
            && StringUtils.endsWith(filename.toAbsolutePath().toString(), ".properties");
    }

    /**
     * 添加文件监听
     */
    private void addFileListener() {
        regListenerForPath(configProfile.getFilemultimap());
    }

    /**
     * 注册文件监听
     * 
     * @param multimap 文件路径map
     */
    private void regListenerForPath(Multimap<String, File> multimap) {
        Iterator<String> iterator = multimap.keySet().iterator();
        while (iterator.hasNext()) {
            executorService.submit(new DirectoryListener(Paths.get(iterator.next())));
        }
    }
}
