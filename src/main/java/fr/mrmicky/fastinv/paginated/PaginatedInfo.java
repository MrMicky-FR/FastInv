package fr.mrmicky.fastinv.paginated;

/**
 * Represents data about a currently open page in a {@link PaginatedFastInv}.
 */
public class PaginatedInfo {
    private final int current;
    private final int maxPages;

    public PaginatedInfo(int current, int maxPages) {
        this.current = current;
        this.maxPages = maxPages;
    }

    public int current() {
        return this.current;
    }

    public int maxPages() {
        return this.maxPages;
    }
}