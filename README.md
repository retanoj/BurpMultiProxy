BurpMultiProxy
=========

> ###Burpsuite 切换代理插件（jar版）

> ###功能：
>     为Intruder模块每次请求随机选取一条代理

> Intruder模块，payload中的url写全路径

>     例如：     
> 
>         GET /ic.asp HTTP/1.1
>         Host: 1111.ip138.com
>         ......
>     
>     修改为 
>
>         GET http://1111.ip138.com/ic.asp HTTP/1.1
>         Host: 1111.ip138.com
>         ......