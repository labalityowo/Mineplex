'use strict'

const t = require('tap')
const test = t.test
const Fastify = require('fastify')
const fastifyMysql = require('../index')

test('fastify.mysql namespace should exist', t => {
  t.plan(9)

  const fastify = Fastify()

  fastify.register(fastifyMysql, {
    connectionString: 'mysql://root@localhost/mysql'
  })

  fastify.ready(err => {
    t.error(err)

    t.ok(fastify.mysql)
    t.ok(fastify.mysql.pool)
    t.ok(fastify.mysql.query)
    t.ok(fastify.mysql.execute)
    t.ok(fastify.mysql.getConnection)
    t.ok(fastify.mysql.format)
    t.ok(fastify.mysql.escape)
    t.ok(fastify.mysql.escapeId)
    fastify.close()
  })
})

test('use query util', t => {
  t.plan(3)

  const fastify = Fastify()

  fastify.register(fastifyMysql, {
    connectionString: 'mysql://root@localhost/mysql'
  })

  fastify.ready(err => {
    t.error(err)

    fastify.mysql.query('SELECT NOW()', (err, result) => {
      t.error(err)

      t.ok(result.length)
      fastify.close()
    })
  })
})

test('use execute util', t => {
  t.plan(3)

  const fastify = Fastify()

  fastify.register(fastifyMysql, {
    connectionString: 'mysql://root@localhost/mysql'
  })

  fastify.ready(err => {
    t.error(err)

    fastify.mysql.execute('SELECT NOW()', (err, result) => {
      t.error(err)

      t.ok(result.length)
      fastify.close()
    })
  })
})

test('use getConnection util', t => {
  t.plan(7)

  const fastify = Fastify()

  fastify.register(fastifyMysql, {
    host: 'localhost',
    user: 'root',
    database: 'mysql',
    connectionLimit: 1
  })

  fastify.ready((err) => {
    t.error(err)

    fastify.mysql.getConnection((err, connection) => {
      t.error(err)

      t.ok(connection)
      connection.query('SELECT 1 AS `ping`', (err, results) => {
        t.error(err)

        t.ok(results[0].ping === 1)
        connection.release()
      })
    })
    // if not call connection.release(), it will block next query
    fastify.mysql.query('SELECT NOW()', (err, result) => {
      t.error(err)

      t.ok(result.length)
      fastify.close()
    })
  })
})

test('fastify.mysql.test namespace should exist', t => {
  t.plan(9)

  const fastify = Fastify()

  fastify.register(fastifyMysql, {
    name: 'test',
    connectionString: 'mysql://root@localhost/mysql'
  })

  fastify.ready(err => {
    t.error(err)

    t.ok(fastify.mysql)
    t.ok(fastify.mysql.test)
    t.ok(fastify.mysql.test.pool)
    t.ok(fastify.mysql.test.execute)
    t.ok(fastify.mysql.test.getConnection)
    t.ok(fastify.mysql.test.format)
    t.ok(fastify.mysql.test.escape)
    t.ok(fastify.mysql.test.escapeId)
    fastify.close()
  })
})

test('synchronous functions', (t) => {
  const fastify = Fastify()

  fastify.register(fastifyMysql, {
    host: 'localhost',
    user: 'root',
    database: 'mysql'
  })

  fastify.ready((err) => {
    t.error(err)

    test('mysql.format', (t) => {
      const sqlString = fastify.mysql.format('SELECT ? AS `now`', [1])
      t.is('SELECT 1 AS `now`', sqlString)
      t.end()
    })

    test('mysql.escape', (t) => {
      const id = 'userId'
      const sql = 'SELECT * FROM users WHERE id = ' + fastify.mysql.escape(id)
      t.is(sql, `SELECT * FROM users WHERE id = '${id}'`)
      t.end()
    })

    test('mysql.escapeId', (t) => {
      const sorter = 'date'
      const sql = 'SELECT * FROM posts ORDER BY ' + fastify.mysql.escapeId('posts.' + sorter)
      t.ok(sql, 'SELECT * FROM posts ORDER BY `posts`.`date`')
      t.end()
    })

    fastify.close()
    t.end()
  })
})
