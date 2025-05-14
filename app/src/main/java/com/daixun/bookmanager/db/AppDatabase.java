package com.daixun.bookmanager.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.daixun.bookmanager.dao.BookDao;
import com.daixun.bookmanager.dao.BorrowDao;
import com.daixun.bookmanager.dao.ReaderDao;
import com.daixun.bookmanager.dao.UserDao;
import com.daixun.bookmanager.model.Book;
import com.daixun.bookmanager.model.Borrow;
import com.daixun.bookmanager.model.Reader;
import com.daixun.bookmanager.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Book.class, Borrow.class, Reader.class, User.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BookDao bookDao();
    public abstract BorrowDao borrowDao();
    public abstract ReaderDao readerDao();
    public abstract UserDao userDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
            
    // 定义从版本1到版本2的迁移，添加Book表的coverUrl字段
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE books ADD COLUMN coverUrl TEXT DEFAULT ''");
        }
    };
    
    // 定义从版本2到版本3的迁移，添加User表的avatarUrl、email和phone字段
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN avatarUrl TEXT DEFAULT ''");
            database.execSQL("ALTER TABLE users ADD COLUMN email TEXT DEFAULT ''");
            database.execSQL("ALTER TABLE users ADD COLUMN phone TEXT DEFAULT ''");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "book_manager_database")
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                UserDao userDao = INSTANCE.userDao();
                // 添加一个默认的管理员账户
                User admin = new User("admin", "admin", true);
                userDao.insert(admin);
                
                // 添加一个默认的学生账户
                User student = new User("student", "student", false);
                userDao.insert(student);
            });
        }
    };
} 