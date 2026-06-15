package io.sclera.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Helpers for wrapping already-fetched collections in a Spring {@link org.springframework.data.domain.Page}
 * envelope without changing the underlying queries or pagination logic.
 *
 * <p>The existing repositories paginate manually inside native SQL (LIMIT/OFFSET driven by
 * {@code pageno}/{@code pagesize} request params) and return bare {@link Collection}s. These helpers keep
 * that behaviour intact and only adapt the controller return type to {@code Page<T>}; the data, SQL and
 * page slicing are unchanged.</p>
 *
 * <p>Grand totals are not recomputed here: where an accurate element count is cheaply available the caller
 * passes it via {@link #toPage(Collection, Integer, Integer, long)}; otherwise {@code totalElements}
 * reflects the returned slice ({@code offset + slice size}) and callers should keep using the existing
 * dedicated count endpoints for full totals.</p>
 */
public final class PageUtils {

    private PageUtils() {
    }

    /**
     * Wraps a fetched page slice in a {@link PageImpl} using an accurate total element count.
     *
     * @param content  the already-fetched slice (may be {@code null})
     * @param pageNo   the 1-based page number from the request
     * @param pageSize the page size from the request
     * @param total    the accurate total number of matching elements
     * @return a page over the supplied slice
     */
    public static <T> PageImpl<T> toPage(Collection<T> content, Integer pageNo, Integer pageSize, long total) {
        List<T> list = toList(content);
        return new PageImpl<>(list, buildPageable(pageNo, pageSize, list.size()), total);
    }

    /**
     * Wraps a fetched page slice in a {@link PageImpl} when no accurate total count is available.
     * {@code totalElements} is reported as {@code offset + slice size}.
     *
     * @param content  the already-fetched slice (may be {@code null})
     * @param pageNo   the 1-based page number from the request
     * @param pageSize the page size from the request
     * @return a page over the supplied slice
     */
    public static <T> PageImpl<T> toPage(Collection<T> content, Integer pageNo, Integer pageSize) {
        List<T> list = toList(content);
        Pageable pageable = buildPageable(pageNo, pageSize, list.size());
        long total = pageable.getOffset() + list.size();
        return new PageImpl<>(list, pageable, total);
    }

    /**
     * Wraps a full, non-paginated collection in a single-page {@link PageImpl}. Used for endpoints that
     * return the entire collection (no {@code pageno}/{@code pagesize}); the full collection becomes page 0.
     *
     * @param content the full collection (may be {@code null})
     * @return a single page containing the whole collection
     */
    public static <T> PageImpl<T> toPage(Collection<T> content) {
        List<T> list = toList(content);
        int size = Math.max(list.size(), 1);
        return new PageImpl<>(list, PageRequest.of(0, size), list.size());
    }

    private static <T> List<T> toList(Collection<T> content) {
        return content == null ? new ArrayList<>() : new ArrayList<>(content);
    }

    private static Pageable buildPageable(Integer pageNo, Integer pageSize, int sliceSizeFallback) {
        int page = (pageNo == null || pageNo < 1) ? 1 : pageNo;
        int size = (pageSize == null || pageSize < 1) ? Math.max(sliceSizeFallback, 1) : pageSize;
        return PageRequest.of(page - 1, size);
    }
}
