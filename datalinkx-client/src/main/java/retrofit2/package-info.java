
/**
 * 用于覆盖retrofit的部分实现 目前的一些扩展不在retrofit的扩展点上 需要修改源码进行支持 增加以下支持(保持和SpringMVC
 * 的功能一致性): 1. jsr305对参数的有效性校验 2. 支持Bean作为条件，将Bean的属性值转换为查询条件
 */
package retrofit2;
