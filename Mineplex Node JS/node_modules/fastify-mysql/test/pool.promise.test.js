'use strict'

const test = require('tap').test
const Fastify = require('fastify')
const fastifyMysql = require('../index')

test('promise pool', (t) => {
  let fastify

  t.beforeEach((done) => {
    fastify = Fastify()
    fastify.register(fastifyMysql, {
      promise: true,
      host: 'localhost',
      user: 'root',
      database: 'mysql',
      connectionLimit: 1
    })
    done()
  })

  t.afterEach((done) => {
    fastify.close()
    done()
  })

  t.test('mysql.pool.query', (t) => {
    t.plan(3)

    fastify.ready((err) => {
      t.error(err)

      fastify.mysql.query('SELECT 1 AS `ping`')
        .then(([results, fields]) => {
          t.ok(results[0].ping === 1)
          t.ok(fields)
        })
    })
  })

  t.test('mysql.pool.execute', (t) => {
    t.plan(3)

    fastify.ready((err) => {
      t.error(err)

      fastify.mysql.execute('SELECT ? AS `ping`', [1])
        .then(([results, fields]) => {
          t.ok(results[0].ping === 1)
          t.ok(fields)
        })
    })
  })

  t.test('promise pool.getConnection', (t) => {
    t.plan(7)

    fastify.ready((err) => {
      t.error(err)

      fastify.mysql.getConnection()
        .then((connection) => {
          connection.query('SELECT 2 AS `ping`')
            .then(([results, fields]) => {
              t.ok(results[0].ping === 2)
              t.ok(fields)
              connection.release()
            })
        })

      fastify.mysql.query('SELECT 3 AS `ping`')
        .then(([results, fields]) => {
          t.ok(results[0].ping === 3)
          t.ok(fields)
        })

      fastify.mysql.execute('SELECT ? AS `ping`', [3])
        .then(([results, fields]) => {
          t.ok(results[0].ping === 3)
          t.ok(fields)
        })
    })
  })

  t.end()
})
