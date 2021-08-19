'use strict'

const test = require('tap').test
const Fastify = require('fastify')
const fastifyMysql = require('../index')

test('fastify.mysql namespace should exist', (t) => {
  const fastify = Fastify()
  fastify.register(fastifyMysql, {
    type: 'connection',
    name: 'test',
    host: 'localhost',
    user: 'root',
    database: 'mysql'
  })

  fastify.ready((err) => {
    t.error(err)
    t.ok(fastify.mysql)
    t.ok(fastify.mysql.test)
    t.ok(fastify.mysql.test.connection)
    t.ok(fastify.mysql.test.query)
    t.ok(fastify.mysql.test.execute)

    t.ok(fastify.mysql.test.format)
    t.ok(fastify.mysql.test.escape)
    t.ok(fastify.mysql.test.escapeId)

    fastify.close()
    t.end()
  })
})

test('utils should work', (t) => {
  let fastify = null
  t.beforeEach((done) => {
    fastify = Fastify()

    fastify.register(fastifyMysql, {
      type: 'connection',
      connectionString: 'mysql://root@localhost/mysql'
    })

    done()
  })

  t.afterEach((done) => {
    fastify.close()
    fastify = null
    done()
  })

  t.test('query util', (t) => {
    fastify.ready((err) => {
      t.error(err)

      fastify.mysql.query('SELECT 1 AS `ping`', (err, results) => {
        t.error(err)
        t.ok(results[0].ping === 1)

        fastify.close()
        t.end()
      })
    })
  })

  t.test('execute util', (t) => {
    fastify.ready((err) => {
      t.error(err)

      fastify.mysql.execute('SELECT ? as `ping`', [1], (err, results) => {
        t.error(err)
        t.ok(results[0].ping === 1)

        fastify.close()
        t.end()
      })
    })
  })

  t.test('format util', (t) => {
    fastify.ready((err) => {
      t.error(err)

      const sqlString = fastify.mysql.format('SELECT ? AS `now`', [1])
      t.is('SELECT 1 AS `now`', sqlString)
      t.end()
    })
  })

  t.test('escape util', (t) => {
    fastify.ready((err) => {
      t.error(err)

      const id = 'userId'
      const sql = 'SELECT * FROM users WHERE id = ' + fastify.mysql.escape(id)
      t.is(sql, `SELECT * FROM users WHERE id = '${id}'`)
      t.end()
    })
  })

  t.test('escapeId util', (t) => {
    fastify.ready((err) => {
      t.error(err)

      const sorter = 'date'
      const sql = 'SELECT * FROM posts ORDER BY ' + fastify.mysql.escapeId('posts.' + sorter)
      t.ok(sql, 'SELECT * FROM posts ORDER BY `posts`.`date`')
      t.end()
    })
  })

  t.end()
})

test('promise connection', (t) => {
  let fastify = null
  t.beforeEach((done) => {
    fastify = Fastify()

    fastify.register(fastifyMysql, {
      promise: true,
      type: 'connection',
      connectionString: 'mysql://root@localhost/mysql'
    })
    done()
  })
  t.afterEach((done) => {
    fastify.close()
    fastify = null
    done()
  })

  t.test('query util', (t) => {
    fastify.ready((err) => {
      t.error(err)

      fastify.mysql.query('SELECT 1 AS `ping`').then(([results]) => {
        t.error(err)

        t.ok(results[0].ping === 1)
        t.end()
      })
    })
  })

  t.test('execute util', (t) => {
    fastify.ready((err) => {
      t.error(err)

      fastify.mysql.execute('SELECT ? AS `ping`', [1]).then(([results]) => {
        t.error(err)

        t.ok(results[0].ping === 1)
        t.end()
      })
    })
  })

  t.test('format util', (t) => {
    fastify.ready((err) => {
      t.error(err)

      const sqlString = fastify.mysql.format('SELECT ? AS `now`', [1])
      t.is('SELECT 1 AS `now`', sqlString)
      t.end()
    })
  })

  t.test('escape util', (t) => {
    fastify.ready((err) => {
      t.error(err)

      const id = 'userId'
      const sql = 'SELECT * FROM users WHERE id = ' + fastify.mysql.escape(id)
      t.is(sql, `SELECT * FROM users WHERE id = '${id}'`)
      t.end()
    })
  })

  t.test('escapeId util', (t) => {
    fastify.ready((err) => {
      t.error(err)

      const sorter = 'date'
      const sql = 'SELECT * FROM posts ORDER BY ' + fastify.mysql.escapeId('posts.' + sorter)
      t.ok(sql, 'SELECT * FROM posts ORDER BY `posts`.`date`')
      t.end()
    })
  })

  t.test('Should throw when mysql2 fail to perform operation', (t) => {
    fastify.ready((err) => {
      t.error(err)

      const sorter = 'date'
      const sql = 'SELECT * FROM posts ORDER BY ' + fastify.mysql.escapeId('posts.' + sorter)
      t.ok(sql, 'SELECT * FROM posts ORDER BY `posts`.`date`')
      t.end()
    })
  })

  t.end()
})

test('Promise: should throw when mysql2 fail to perform operation', (t) => {
  t.plan(3)

  const fastify = Fastify()
  t.teardown(() => fastify.close())

  fastify.register(fastifyMysql, {
    type: 'connection',
    name: 'test',
    host: 'localhost',
    user: 'root',
    database: 'mysql',
    promise: true
  })

  fastify.ready((err) => {
    t.error(err)

    const sql = 'SELECT fastify FROM fastify'

    fastify.mysql.test.connection.query(sql).catch((errors) => {
      t.ok(errors)
      t.is(errors.message, "Table 'mysql.fastify' doesn't exist")
    })
  })
})
