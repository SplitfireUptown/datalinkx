import {
  mysqlPng,
  oraclePng,
  ESPng,
  redisPng
  // httpPng
} from '@/core/icons'

const DataSourceType = [
  {
    label: 'MySQL',
    value: 1
  },
  {
    label: 'ELASTICSEARCH',
    value: 2
  },
  {
    label: 'ORACLE',
    value: 3
  },
  {
    label: 'REDIS',
    value: 4
  }
]
const dsTypeList = [
  {
    value: 'MySQL',
    label: 'MySQL',
    dsTypeKey: 1,
    img: mysqlPng
  },
  {
    value: 'es',
    label: 'ES',
    dsTypeKey: 2,
    img: ESPng
  },
  {
    value: 'oracle',
    label: 'Oracle',
    dsTypeKey: 3,
    img: oraclePng
  },
  {
    value: 'redis',
    label: 'Redis',
    dsTypeKey: 4,
    img: redisPng
  }
  // {
  //   value: 'http',
  //   label: 'Http',
  //   dsTypeKey: 4,
  //   img: httpPng
  // }
]
// 目标数据源 redis 类型
const RedisTypes = [
  {
    label: 'string',
    value: 'string'
  },
  {
    label: 'list-lpush',
    value: 'list-lpush'
  },
  {
    label: 'list-rpush',
    value: 'list-rpush'
  },
  {
    label: 'set-sadd',
    value: 'set-sadd'
  },
  {
    label: 'zset-zadd',
    value: 'zset-zadd'
  },
  {
    label: 'hash-hset',
    value: 'hash-hset'
  }
]

export { DataSourceType, dsTypeList, RedisTypes }
