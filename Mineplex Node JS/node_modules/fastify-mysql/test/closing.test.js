const test = require('tap').test
const Fastify = require('fastify')
const fastifyMysql = require('../index')

test('callback connection', (t) => {
  let fastify = null

  fastify = Fastify()
  fastify.register(fastifyMysql, {
    type: 'connection',
    connectionString: 'mysql://root@localhost/mysql'
  })

  fastify.ready((err) => {
    t.error(err)

    fastify.mysql.query('SELECT 1 AS `ping`', (err, results) => {
      t.error(err)
      t.ok(results[0].ping === 1)

      fastify.close((closeErr) => {
        t.error(closeErr)
        t.end()
      })
    })
  })
})

test('promise connection', (t) => {
  let fastify = null

  fastify = Fastify()
  fastify.register(fastifyMysql, {
    promise: true,
    type: 'connection',
    connectionString: 'mysql://root@localhost/mysql'
  })

  fastify.ready((err) => {
    t.error(err)

    fastify.mysql.query('SELECT 1 AS `ping`').then(([results]) => {
      t.ok(results[0].ping === 1)

      fastify.close((closeErr) => {
        t.error(closeErr)
        t.end()
      })
    })
  })
})
