# middleware-container
基于tomcat实现一个解决多个中间件依赖jar包与业务jar包隔离的容器，不仅仅解决各个中间件之间的依赖隔离，也解决业务与中间件的依赖隔离

其实现机制及源码分析见如下博客：


[容器隔离机制实现1-概述](http://www.jianshu.com/p/96ea382c82a8)

[容器隔离机制实现2-类加载器介绍](http://www.jianshu.com/p/6320268b474b)

[容器隔离机制实现3-容器隔离实现架构](http://www.jianshu.com/p/98b0922003b7)

[容器隔离机制实现4-源码分析](http://www.jianshu.com/p/886cfc2cfae8)
