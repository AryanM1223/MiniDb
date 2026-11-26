import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {
    private static int passed = 0;
    private static int failed = 0;

    private interface Action {
        void run() throws Exception;
    }

    public static void main(String[] args) throws IOException {
        String path = "test.db";
        new File(path).delete();

        DiskManager dm = new DiskManager(path);

        int p0 = dm.allocatePage();
        int p1 = dm.allocatePage();
        int p2 = dm.allocatePage();
        check("allocate returns 0, 1, 2", p0 == 0 && p1 == 1 && p2 == 2);
        check("page count is 3", dm.getPageCount() == 3);
        check("file size is 12288 bytes", new File(path).length() == 12288);

        Page page = dm.readPage(1);
        check("read page has correct id", page.getPageId() == 1);

        byte[] r0 = bytes("alice");
        byte[] r1 = bytes("bob");
        byte[] r2 = bytes("charlie");
        int s0 = page.insertRecord(r0);
        int s1 = page.insertRecord(r1);
        int s2 = page.insertRecord(r2);
        check("slot ids are 0, 1, 2", s0 == 0 && s1 == 1 && s2 == 2);
        check("record 0 reads back", Arrays.equals(page.getRecord(s0), r0));
        check("record 2 reads back", Arrays.equals(page.getRecord(s2), r2));

        dm.writePage(page);
        dm.close();

        dm = new DiskManager(path);
        final DiskManager disk = dm;

        Page reloaded = disk.readPage(1);
        check("page count survives reopen", disk.getPageCount() == 3);
        check("slot count survives reopen", reloaded.getSlotCount() == 3);
        check("record 1 survives reopen", Arrays.equals(reloaded.getRecord(1), r1));

        check("delete returns true", reloaded.deleteRecord(1));
        check("deleted record returns null", reloaded.getRecord(1) == null);
        check("deleting twice returns false", !reloaded.deleteRecord(1));
        check("record 0 still there after delete", Arrays.equals(reloaded.getRecord(0), r0));

        int freeBefore = reloaded.getFreeSpace();
        reloaded.compact();
        check("record 0 survives compact", Arrays.equals(reloaded.getRecord(0), r0));
        check("record 2 survives compact", Arrays.equals(reloaded.getRecord(2), r2));
        check("record 1 still deleted after compact", reloaded.getRecord(1) == null);
        check("compact frees the deleted bytes", reloaded.getFreeSpace() == freeBefore + r1.length);

        Page fill = disk.readPage(2);
        byte[] big = new byte[100];
        int inserted = 0;
        while (fill.insertRecord(big) != -1) {
            inserted++;
        }
        check("exactly 38 records of 100 bytes fit", inserted == 38);

        checkThrows("read page -1 throws", () -> disk.readPage(-1));
        checkThrows("read page 3 throws", () -> disk.readPage(3));
        Page outOfRange = new Page((byte) 1, 50);
        checkThrows("write page 50 throws", () -> disk.writePage(outOfRange));
        check("no gap pages were created", disk.getPageCount() == 3);

        disk.close();

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
    }

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    private static void checkThrows(String name, Action action) {
        try {
            action.run();
            check(name, false);
        } catch (IllegalArgumentException e) {
            check(name, true);
        } catch (Exception e) {
            check(name, false);
        }
    }
}