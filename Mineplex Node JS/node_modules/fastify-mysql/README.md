# fastify-mysql

![CI workflow](https://github.com/fastify/fastify-mysql/workflows/CI%20workflow/badge.svg)
[![js-standard-style](https://img.shields.io/badge/code%20style-standard-brightgreen.svg?style=flat)](http://standardjs.com/)

Fastify MySQL connection plugin, with this you can share the same MySQL connection pool in every part of your server.
Under the hood the [mysql2](https://github.com/sidorares/node-mysql2) is used, the options that you pass to `register` will be passed to the MySQL pool builder.

## Install
```
npm i fastify-mysql --save
```
## Usage
Add it to you project with `register` and you are done!
This plugin will add the `mysql` namespace in your Fastify instance, with the following properties:
```
pool: the pool instance
query: an utility to perform a query without a transaction
execute: an utility to perform a prepared statement without a transaction
getConnection: get a connection from the pool
format: an utility to generate SQL string
escape: an utility to escape query values
escapeId: an utility to escape query identifiers
```

Example:
```js
const fastify = require('fastify')()

fastify.register(require('fastify-mysql'), {
  connectionString: 'mysql://root@localhost/mysql'
})

fastify.get('/user/:id', (req, reply) => {
  fastify.mysql.getConnection(onConnect)

  function onConnect (err, client) {
    if (err) return reply.send(err)

    client.query(
      'SELECT id, username, hash, salt FROM users WHERE id=?', [req.params.id],
      function onResult (err, result) {
        client.release()
        reply.send(err || result)
      }
    )
  }
})

fastify.listen(3000, err => {
  if (err) throw err
  console.log(`server listening on ${fastify.server.address().port}`)
})
```

Use of `mysql.query`
```js
const fastify = require('fastify')()

fastify.register(require('fastify-mysql'), {
  connectionString: 'mysql://root@localhost/mysql'
})

fastify.get('/user/:id', (req, reply) => {
  fastify.mysql.query(
    'SELECT id, username, hash, salt FROM users WHERE id=?', [req.params.id],
    function onResult (err, result) {
      reply.send(err || result)
    }
  )
})

fastify.listen(3000, err => {
  if (err) throw err
  console.log(`server listening on ${fastify.server.address().port}`)
})
```
As you can see there is no need to close the client, since is done internally.

Async/await is supported, when register `promise` option is `true`:
```js
const fastify = require('fastify')()

fastify.register(require('fastify-mysql'), {
  promise: true,
  connectionString: 'mysql://root@localhost/mysql'
})

fastify.get('/user/:id', async (req, reply) => {
  const connection = await fastify.mysql.getConnection()
  const [rows, fields] = await connection.query(
    'SELECT id, username, hash, salt FROM users WHERE id=?', [req.params.id],
  )
  connection.release()
  return rows[0]
})

fastify.listen(3000, err => {
  if (err) throw err
  console.log(`server listening on ${fastify.server.address().port}`)
})
```

## TypeScript
As `mysql2` expose four different type of client, we do not specify the typing for you. You need to specify the type yourself follow by the example below.
```ts
import { MySQLConnection, MySQLPool, MySQLPromiseConnection, MySQLPromisePool } from 'fastify-mysql'

// if you only pass connectionString
declare module 'fastify' {
  interface FastifyInstance {
    mysql: MySQLPool 
  }
}

// if you passed type = 'connection'
declare module 'fastify' {
  interface FastifyInstance {
    mysql: MySQLConnection 
  }
}

// if you passed promise = true
declare module 'fastify' {
  interface FastifyInstance {
    mysql: MySQLPromisePool 
  }
}

// if you passed promise = true, type = 'connection'
declare module 'fastify' {
  interface FastifyInstance {
    mysql: MySQLPromiseConnection 
  }
}
```

## Acknowledgements

This project is kindly sponsored by:
- [nearForm](http://nearform.com)
- [LetzDoIt](http://www.letzdoitapp.com/)

## License

Licensed under [MIT](./LICENSE).
