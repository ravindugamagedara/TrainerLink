package module;

public class HomeSliderItem {

    private final int imageResId;
    private final String title;
    private final String quote;

    public HomeSliderItem(int imageResId, String title, String quote) {
        this.imageResId = imageResId;
        this.title = title;
        this.quote = quote;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getQuote() {
        return quote;
    }
}