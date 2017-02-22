package cn.woblog.android.downloader.db;

import static cn.woblog.android.downloader.domain.DownloadInfo.STATUS_COMPLETED;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.woblog.android.downloader.domain.DownloadInfo;
import cn.woblog.android.downloader.domain.DownloadThreadInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by renpingqing on 17/1/23.
 */

public class DefaultDownloadDBController implements DownloadDBController {


  public static final String[] DOWNLOAD_INFO_COLUMNS = new String[]{"_id", "supportRanges",
      "createAt", "uri",
      "path", "size", "progress",
      "status"};

  public static final String[] DOWNLOAD_THREAD_INFO_COLUMNS = new String[]{"_id", "threadId",
      "downloadInfoId", "uri",
      "start", "end", "progress"};

  private final Context context;
  private final DefaultDownloadHelper dbHelper;
  private final SQLiteDatabase writableDatabase;
  private final SQLiteDatabase readableDatabase;

  public DefaultDownloadDBController(Context context) {
    this.context = context;
    dbHelper = new DefaultDownloadHelper(context);
    writableDatabase = dbHelper.getWritableDatabase();
    readableDatabase = dbHelper.getReadableDatabase();
  }

  @SuppressWarnings("No problem")
  @Override
  public List<DownloadInfo> findAllDownloading() {
    Cursor cursor = readableDatabase.query("download_info",
        DOWNLOAD_INFO_COLUMNS, "status!=?", new String[]{
            String.valueOf(STATUS_COMPLETED)}, null, null, "createAt desc");

    List<DownloadInfo> downloads = new ArrayList<>();
    Cursor downloadCursor;
    while (cursor.moveToNext()) {
      DownloadInfo downloadInfo = new DownloadInfo();
      downloads.add(downloadInfo);

      inflateDownloadInfo(cursor, downloadInfo);

      //query download thread info
      downloadCursor = readableDatabase.query("download_thread_info",
          DOWNLOAD_THREAD_INFO_COLUMNS, "downloadInfoId=?", new String[]{
              String.valueOf(downloadInfo.getId())}, null, null, null);
      List<DownloadThreadInfo> downloadThreads = new ArrayList<>();
      while (downloadCursor.moveToNext()) {
        DownloadThreadInfo downloadThreadInfo = new DownloadThreadInfo();
        downloadThreads.add(downloadThreadInfo);
        inflateDownloadThreadInfo(downloadCursor, downloadThreadInfo);
      }

      downloadInfo.setDownloadThreadInfos(downloadThreads);

    }
    return downloads;
  }

  private void inflateDownloadThreadInfo(Cursor cursor,
      DownloadThreadInfo downloadThreadInfo) {
    downloadThreadInfo.setId(cursor.getInt(0));
    downloadThreadInfo.setThreadId(cursor.getInt(1));
    downloadThreadInfo.setDownloadInfoId(cursor.getInt(2));
    downloadThreadInfo.setUri(cursor.getString(3));
    downloadThreadInfo.setStart(cursor.getLong(4));
    downloadThreadInfo.setEnd(cursor.getLong(5));
    downloadThreadInfo.setProgress(cursor.getLong(6));
  }

  private void inflateDownloadInfo(Cursor cursor, DownloadInfo downloadInfo) {
    downloadInfo.setId(cursor.getInt(0));
    downloadInfo.setSupportRanges(cursor.getInt(1));
    downloadInfo.setCreateAt(cursor.getLong(2));
    downloadInfo.setUri(cursor.getString(3));
    downloadInfo.setPath(cursor.getString(4));
    downloadInfo.setSize(cursor.getLong(5));
    downloadInfo.setProgress(cursor.getLong(6));
    downloadInfo.setStatus(cursor.getInt(7));
  }

  @Override
  public DownloadInfo findDownloadedInfoById(int id) {
    Cursor cursor = readableDatabase
        .query("download_info", DOWNLOAD_INFO_COLUMNS, "_id=?", new String[]{String.valueOf(id)},
            null, null, "createAt desc");
    if (cursor.moveToNext()) {
      DownloadInfo downloadInfo = new DownloadInfo();

      inflateDownloadInfo(cursor, downloadInfo);

      return downloadInfo;
    }
    return null;
  }

  @Override
  public void createOrUpdate(DownloadInfo downloadInfo) {
    writableDatabase.execSQL(
        "REPLACE INTO download_info(_id,supportRanges,createAt,uri,path,size,progress,status) VALUES(?,?,?,?,?,?,?,?);",
        new Object[]{
            downloadInfo.getId(), downloadInfo.getSupportRanges(),
            downloadInfo.getCreateAt(), downloadInfo.getUri(), downloadInfo.getPath(),
            downloadInfo.getSize(), downloadInfo.getProgress(), downloadInfo.getStatus()});
  }

  @Override
  public void createOrUpdate(DownloadThreadInfo downloadThreadInfo) {
    writableDatabase.execSQL(
        "REPLACE INTO download_thread_info(_id,threadId,downloadInfoId,uri,start,end,progress) VALUES(?,?,?,?,?,?,?);",
        new Object[]{
            downloadThreadInfo.getId(),
            downloadThreadInfo.getThreadId(),
            downloadThreadInfo.getDownloadInfoId(),
            downloadThreadInfo.getUri(),
            downloadThreadInfo.getStart(), downloadThreadInfo.getEnd(),
            downloadThreadInfo.getProgress()});
  }

  @Override
  public void delete(DownloadInfo downloadInfo) {

  }

  @Override
  public void delete(DownloadThreadInfo download) {

  }
}
