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

const dsConfigOriginList = [
  {
    label: '数据源名称',
    key: 'name',
    type: 'input',
    decorator: '请输入数据源名称',
    isRequired: true
  },
  {
    label: 'host',
    key: 'host',
    type: 'input',
    decorator: '请输入 host',
    isRequired: true
  },
  {
    label: 'port',
    key: 'port',
    type: 'input',
    decorator: '请输入 port',
    isRequired: true
  },
  {
    label: 'username',
    key: 'username',
    type: 'input',
    decorator: '请输入 username',
    isRequired: true
  },
  {
    label: 'password',
    key: 'password',
    type: 'password',
    decorator: '请输入 password',
    isRequired: true
  },
  {
    label: 'database',
    key: 'database',
    type: 'input',
    decorator: '请输入 database',
    isRequired: true
  }
  // {
  //   label: '附加配置',
  //   key: 'config',
  //   type: 'textarea',
  //   decorator: '请输入附加配置',
  //   isRequired: false
  // }
]

const OracleServerTypes = [
  {
    label: 'SID',
    value: 'sid'
  }
]

export { DataSourceType, dsTypeList, RedisTypes, dsConfigOriginList, OracleServerTypes }
