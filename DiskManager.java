import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DiskManager {
    private static final int PAGE_SIZE = 4096;
    private File dbFile;
    private RandomAccessFile file;

    public DiskManager(String filePath) throws IOException{
        this.dbFile = new File(filePath);
        this.file = new RandomAccessFile(dbFile,"rw");
    }

    public int getPageCount() throws IOException {
        return (int)(file.length()/PAGE_SIZE);
    }

    public void writePage(Page page) throws IOException {
        int pageId = page.getPageId();
        if(pageId < 0 || pageId >= getPageCount()){
            throw new IllegalArgumentException("Invalid pageId: " + pageId);
        }
        long offset = (long)pageId * PAGE_SIZE;
        file.seek(offset);
        file.write(page.getData());
    }

    public Page readPage(int pageId) throws IOException{
        if(pageId < 0 || pageId >= getPageCount()){
            throw new IllegalArgumentException("Invalid pageId: " + pageId);
        }
        long offset = (long)pageId*PAGE_SIZE;
        file.seek(offset);
        byte[] data = new byte[PAGE_SIZE];
        file.readFully(data);
        return new Page(data);
    }

    public int allocatePage() throws IOException {
        int pageId = getPageCount();
        Page page = new Page((byte) 1, pageId);
        long offset = (long)pageId * PAGE_SIZE;
        file.seek(offset);
        file.write(page.getData());
        return pageId;
    }

    public void close() throws IOException {
        if (file != null) {
            file.close();
        }
    }
}