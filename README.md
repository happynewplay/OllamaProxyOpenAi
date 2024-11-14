### 接口格式转换
这是一个使用ollama接口数据格式代理openai数据格式的应用程序,目前有很多oenai数据格式代理其他模型的数据格式类型,目前项目建议与oneapi进行对接,这样能实现目前所有的模型,但是项目对oneapi的部分模型返回格式解析出现错误
现在很多工具都是支持ollama的,但是ollama仅支持本地模型,如果想使用线上模型但是又不想部署ollama的本地模型.就可以使用这个代理来做了
例如:![148R I7AI{1$4FJ7UFQ22VG](https://github.com/user-attachments/assets/ca2778ff-51db-4f44-b55e-25f1a810f7d7)

![P$872~3T3 21M8{7I0HVM0S](https://github.com/user-attachments/assets/c300b302-fbac-4a3a-b6fe-0be15f771e6a)
这里我结合oneapi就可以使用自己本地的聚合模型平台,然后可以自定义模型的名称.但是后面映射又可以是其他模型
