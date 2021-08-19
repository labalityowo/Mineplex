import { FastifyPluginCallback } from "fastify";
import {
  Connection,
  ConnectionOptions,
  escape,
  format,
  Pool,
  PoolOptions,
} from "mysql2";
import {
  Connection as PromiseConnection,
  Pool as PromisePool,
} from "mysql2/promise";

// upstream package missed type
type escapeId = (val: any, forbidQualified?: boolean) => string;

interface BaseClient {
  format: typeof format;
  escape: typeof escape;
  escapeId: escapeId;
}

export type MySQLConnection = Pick<Connection, "query" | "execute"> & {
  connection: Connection;
} & BaseClient;

export type MySQLPool = Pick<Pool, "query" | "execute" | "getConnection"> & {
  pool: Pool;
} & BaseClient;

export type MySQLPromiseConnection = Pick<
  PromiseConnection,
  "query" | "execute"
> & {
  connection: PromiseConnection;
} & BaseClient;

export type MySQLPromisePool = Pick<
  PromisePool,
  "query" | "execute" | "getConnection"
> & {
  pool: PromisePool;
} & BaseClient;

export type ConnectionType = "connection" | "pool";

export interface MySQLOptions extends PoolOptions, ConnectionOptions {
  type?: ConnectionType;
  name?: string;
  promise?: boolean;
  connectionString?: string;
}

export const fastifyMySQL: FastifyPluginCallback<MySQLOptions>;
export default fastifyMySQL;
