### Interface format conversion
This is an application that uses the Ollama interface data format to proxy the OpenAI data format. Currently, there are many types of OpenAI data format proxies for other models. The project currently suggests integrating with OneAPI, which would allow the implementation of all models. However, the project encounters parsing errors with the return formats of some OneAPI models. Many tools now support Ollama, but Ollama only supports local models. If you want to use online models without deploying local models for Ollama, you can use this proxy to achieve that.
For example:![148R I7AI{1$4FJ7UFQ22VG](https://github.com/user-attachments/assets/ca2778ff-51db-4f44-b55e-25f1a810f7d7)

![P$872~3T3 21M8{7I0HVM0S](https://github.com/user-attachments/assets/c300b302-fbac-4a3a-b6fe-0be15f771e6a)
Here, I can integrate OneAPI to use my local aggregated model platform and then customize the model names. However, the mapping can later be for other models.
