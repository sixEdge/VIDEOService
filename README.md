### 主要构件
Controller: 一个 controller 是一个类，用 @Controller 注解标识，对应一种角色或一个场景的功能集合。

Action: 一个 action 是一个 controller 类中的一个方法，用 @Get 或 @Path 注解标识，对应一个由请求路径[+请求参数]所唯一标识的后台功能。

Service: 一个 service 是一个类，对应一个或多个业务流程的组合。

Dao: 一个 dao 是一个类，对应一种或多种数据读写方式。