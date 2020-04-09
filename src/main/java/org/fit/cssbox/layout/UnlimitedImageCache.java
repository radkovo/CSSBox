package org.fit.cssbox.layout;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of the image cache with unlimited storage size.
 *
 * @author dedrakot. Based on Alessandro Tucci simple cache for storing already loaded images.
 */
public class UnlimitedImageCache implements ImageCache
{
    private static ConcurrentHashMap<URL, ContentImage> cache;

    static {
        cache = new ConcurrentHashMap<>();
    }

    private static ConcurrentHashMap<URL, Boolean> failed;

    static {
        failed = new ConcurrentHashMap<>();
    }

    @Override
    public void put(URL uri, ContentImage image)
    {
        cache.put(uri, image);
    }

    @Override
    public ContentImage get(URL uri)
    {
        return cache.get(uri);
    }

    @Override
    public void putFailed(URL uri)
    {
        failed.put(uri, Boolean.TRUE);
    }

    @Override
    public boolean hasFailed(URL uri)
    {
        return failed.get(uri) != null;
    }
}
