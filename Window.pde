import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.User32;

//The following import throw up errors, but actually work, and are necessary (maybe should investigate).
import com.sun.jna.platform.win32.WinBase.PROCESS_INFORMATION;
import com.sun.jna.platform.win32.WinBase.STARTUPINFO;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.INT_PTR;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;

/**
 * This is a wrapper for the HWND type that is in JNA 4.0.
 * Based on a class written by  John Henckel, Oct 2014
 * which can be found at: https://answers.launchpad.net/sikuli/+question/255504
 */
public class Window
{
    public HWND hWnd;

    public Window(HWND hwnd)
    {
        this.hWnd = hwnd;
    }

    public boolean isNull()
    {
        return hWnd == null || hWnd.hashCode() == 0;
    }

    public Window getForegroundWindow()
    {
        return new Window(User32.INSTANCE.GetForegroundWindow());
    }

    public void setForeground()
    {
        User32.INSTANCE.SetForegroundWindow(hWnd);
        minimize();
        restore();
    }

    /**
     * Flash the window three times
     */
    public void flash()
    {
        WinUser.FLASHWINFO pfwi = new WinUser.FLASHWINFO();
        pfwi.cbSize = 20;
        pfwi.hWnd = hWnd;
        pfwi.dwFlags = 3; // 3 = FLASHW_ALL
        pfwi.uCount = 3;
        pfwi.dwTimeout = 250;
        User32.INSTANCE.FlashWindowEx(pfwi);
        sleep(1000);
    }

    /**
     * Returns the class of the window, such as "SWT_Window0" for any SWT application. See AutoIt Window Info Tool
     */
    public String getClassName()
    {
        char[] buffer = new char[2048];
        User32.INSTANCE.GetClassName(hWnd, buffer, 1024);
        return Native.toString(buffer);
    }

    public boolean isVisible()
    {
        return User32.INSTANCE.IsWindowVisible(hWnd);
    }

    /**
     * 3=SW_MAXIMIZE, etc
     */
    private void showWindow(int nCmdShow)
    {
        User32.INSTANCE.ShowWindow(hWnd, nCmdShow);
    }

    public void minimize()
    {
        showWindow(6);
    }

    public void maximize()
    {
        showWindow(3);
    }

    /**
     * Activate and displays the window in normal mode (not minimized or maximized)
     */
    public void restore()
    {
        showWindow(9);
    }

    /**
     * Return true if the window message queue is idle, false if timeout
     */
    public boolean waitForInputIdle(int timeout_ms)
    {
        IntByReference lpdwProcessId = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hWnd, lpdwProcessId);
        return User32.INSTANCE.WaitForInputIdle(new HANDLE(lpdwProcessId.getPointer()), new DWORD(timeout_ms)).intValue() == 0;
    }

    public void setRectangle(Rectangle rect)
    {
        User32.INSTANCE.MoveWindow(hWnd, rect.x, rect.y, rect.width, rect.height, true);
    }

    public String getTitle()
    {
        char[] buffer = new char[2048];
        User32.INSTANCE.GetWindowText(hWnd, buffer, 1024);
        return Native.toString(buffer);
    }

    public int getProcessID()
    {
        IntByReference processID = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hWnd, processID);
        return processID.getValue();
    }

    public Rectangle getRectangle()
    {
        RECT rect = new RECT();
        User32.INSTANCE.GetWindowRect(hWnd, rect);
        return new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
    }

    /**
     * In windows the user can customize the border and title bar size.
     * @return x = border thickness in pixels, y = x + titlebar height
     */
    public Point getClientOffset()
    {
        return new Point(getBorderSize(), getTitleHeight());
    }

    /**
     * In windows the user can customize the border and title bar size.
     * @return titlebar height
     */
    public int getTitleHeight()
    {
        int f = User32.INSTANCE.GetSystemMetrics(33); // 33 = SM_CYSIZEFRAME
        return User32.INSTANCE.GetSystemMetrics(4) + f; // 4 = SM_CYCAPTION
    }

    /**
     * In windows the user can customize the border and title bar size.
     * @return border thickness in pixels
     */
    public int getBorderSize()
    {
        int f = User32.INSTANCE.GetSystemMetrics(32); // 32 = SM_CXSIZEFRAME
        return User32.INSTANCE.GetSystemMetrics(5) + f; // 5 = SM_CXBORDER
    }

    /**
     * Find the FIRST top level window with specified class and title.
     * @param className such as "SWT_Window0" or null
     * @param title such as "QREADS" or null
     * @return first window found, or null if not found.
     */
    public Window findWindow(String className, String title)
    {
        return new Window(User32.INSTANCE.FindWindow(className, title));
    }

    /**
     * Get the next top-level window in Z-order, (from foreground to background).
     *
     * To iterate all windows, use
     *
     * for (Window w = getForegroundWindow(); !w.isNull(); w = w.next()) ...
     *
     * @return
     */
    public Window next()
    {
        return new Window(User32.INSTANCE.GetWindow(hWnd, new DWORD(2))); // 2 = GW_HWNDNEXT
    }

    /**
     * This is used to gather results of EnumWindows
     */
    private class WindowList implements WinUser.WNDENUMPROC
    {
        ArrayList<Window> list = new ArrayList<Window>();
        Pattern titlePattern = null;
        int processID;

        @Override
        public boolean callback(HWND hWnd, Pointer data)
        {
            Window w = new Window(hWnd);
            if (titlePattern == null || titlePattern.matcher(w.getTitle()).matches())
            {
                if (processID == 0 || processID == w.getProcessID())
                {
                    list.add(new Window(hWnd));
                    if (processID > 0)
                    {
                        return false; // if matching processID, only need one result
                    }
                }
            }
            return true; // keep going
        }

        // Convert the list into an ordinary array
        Window[] toArray()
        {
            return list.toArray(new Window[list.size()]);
        }

    }

    public  Window[] getTopLevelWindows()
    {
        WindowList result = new WindowList();
        User32.INSTANCE.EnumWindows(result, null);
        return result.toArray();
    }

    public  Window[] getTopLevelWindows(String titleRegex)
    {
        WindowList result = new WindowList();
        result.titlePattern = Pattern.compile(titleRegex);
        User32.INSTANCE.EnumWindows(result, null);
        return result.toArray();
    }

    public Window[] getChildren()
    {
        WindowList result = new WindowList();
        User32.INSTANCE.EnumChildWindows(hWnd, result, null);
        return result.toArray();
    }

    public  Window getProcessWindow(int processID)
    {
        WindowList result = new WindowList();
        result.processID = processID;
        User32.INSTANCE.EnumWindows(result, null);
        return result.list.size() > 0 ? result.list.get(0) : null;
    }

    /**
     * Sleep for milliseconds.
     */
    public  void sleep(int milliseconds)
    {
        try
        {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private  long getProcessHandle(Process process)
    {
        String n = process.getClass().getName();
        if (n.equals("java.lang.ProcessImpl") || n.equals("java.lang.Win32Process"))
        {
            try
            {
                Field field = process.getClass().getDeclaredField("handle");
                field.setAccessible(true);
                return field.getLong(process);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public  int getProcessID(Process process)
    {
        long id = getProcessHandle(process);
        if (id != 0)
        {
            HANDLE handle = new HANDLE();
            handle.setPointer(Pointer.createConstant(id));
            return Kernel32.INSTANCE.GetProcessId(handle);
        }
        return 0;

    }

    /**
     * Runs a program and returns the process
     */
    public  Process runCommand(List<String> cmdline) throws IOException
    {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(cmdline).redirectErrorStream(true);
        return builder.start();
    }

    /**
     * Runs a program and returns the process
     */
    public  Process runCommand(String... cmdline) throws IOException
    {
        return runCommand(Arrays.asList(cmdline));
    }

    /**
     * Runs a program and returns the process id. Note: use runCommand instead of this.
     */
     /*
    public  int createProcess(String program, String args, String currentDirectory)
    {
        STARTUPINFO startupInfo = new STARTUPINFO(); // input
        PROCESS_INFORMATION processInformation = new PROCESS_INFORMATION(); // output
        String cmdline = (args == null || args.length() == 0) ? null : program + " " + args;
        boolean ok = Kernel32.INSTANCE.CreateProcess(program, cmdline, null, null, false, new DWORD(0), null, currentDirectory, startupInfo, processInformation);
        if (!ok) System.out.println("CreateProcess failed err="+Kernel32.INSTANCE.GetLastError());
        return processInformation.dwProcessId.intValue();
    }
*/
    /**
     * Note: use runCommand instead of this, if possible.
     * Use this to open a program or any kind of file, like pdf, jpeg, html, etc.
     * @return true on success.
     */
    public  boolean shellExecute(String filename, String args, String currentDirectory)
    {
        String verb = null; // possible values 'open' (or null), 'edit', 'print', etc.
        INT_PTR intPtr = Shell32.INSTANCE.ShellExecute(null, verb, filename, args, currentDirectory, 1); // 1=SW_SHOWNORMAL
        int rc = intPtr.intValue();
        if (rc <= 32) System.out.println("ShellExecute failed err="+rc+" for filename="+filename);
        return rc > 32;
    }

}