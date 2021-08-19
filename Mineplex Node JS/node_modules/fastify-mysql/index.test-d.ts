import fastify from "fastify";
import fastifyMysql, {
  MySQLConnection,
  MySQLPool,
  MySQLPromiseConnection,
  MySQLPromisePool,
} from ".";

declare module "fastify" {
  interface FastifyInstance {
    mysql:
      | MySQLPool
      | MySQLConnection
      | MySQLPromisePool
      | MySQLPromiseConnection;
  }
}

const app = fastify();
app
  .register(fastifyMysql, {
    connectionString: "mysql://root@localhost/mysql",
  })
  .after(function (err) {
    const mysql = app.mysql as MySQLPool;
    mysql.escapeId("foo");
    mysql.escape("bar");
    mysql.format("baz");
    mysql.query("SELECT NOW()", function () {});
    mysql.execute("SELECT NOW()", function () {});
    mysql.getConnection(function (err, con) {
      con.release();
    });
    mysql.pool.end();
  });

app
  .register(fastifyMysql, {
    promise: true,
    connectionString: "mysql://root@localhost/mysql",
  })
  .after(async function (err) {
    const mysql = app.mysql as MySQLPromisePool;
    mysql.escapeId("foo");
    mysql.escape("bar");
    mysql.format("baz");
    await mysql.query("SELECT NOW()");
    await mysql.execute("SELECT NOW()");
    const con = await mysql.getConnection();
    con.release();
    mysql.pool.end();
  });

app
  .register(fastifyMysql, {
    type: "connection",
    connectionString: "mysql://root@localhost/mysql",
  })
  .after(async function (err) {
    const mysql = app.mysql as MySQLConnection;
    mysql.escapeId("foo");
    mysql.escape("bar");
    mysql.format("baz");
    mysql.query("SELECT NOW()", function () {});
    mysql.execute("SELECT NOW()", function () {});
    mysql.connection.end();
  });

app
  .register(fastifyMysql, {
    type: "connection",
    promise: true,
    connectionString: "mysql://root@localhost/mysql",
  })
  .after(async function (err) {
    const mysql = app.mysql as MySQLPromiseConnection;
    mysql.escapeId("foo");
    mysql.escape("bar");
    mysql.format("baz");
    await mysql.query("SELECT NOW()");
    await mysql.execute("SELECT NOW()");
    mysql.connection.end();
  });
