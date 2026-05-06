package sqlite;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitOffsetLimitHandler;

/**
 * Minimal SQLite dialect for Hibernate 6.x.
 * Adds identity support and tells Hibernate that SQLite supports foreign key
 * constraints in CREATE TABLE so Hibernate generates FK clauses inline
 * instead of issuing ALTER TABLE ... ADD CONSTRAINT (which SQLite doesn't support).
 */
public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();
    }

    @Override
    public LimitHandler getLimitHandler() {
        return LimitOffsetLimitHandler.INSTANCE;
    }

    @Override
    public org.hibernate.dialect.identity.IdentityColumnSupport getIdentityColumnSupport() {
        return new SQLiteIdentityColumnSupport();
    }
    public boolean supportsSelectForUpdate() {
        return false;
    }
    private static class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {
        @Override
        public boolean supportsIdentityColumns() {
            return true;
        }

        @Override
        public String getIdentityColumnString(int type) {
            return "integer";
        }

        @Override
        public String getIdentitySelectString(String table, String column, int type) {
            return "select last_insert_rowid()";
        }
    }
}
