(function (win) {
  axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
  // 创建axios实例
  const service = axios.create({
    // axios中请求配置有baseURL选项，表示请求URL公共部分
    baseURL: '/',
    // 超时
    timeout: 10000
  })
  // request拦截器
  service.interceptors.request.use(config => {
    // 是否需要设置 token
    // const isToken = (config.headers || {}).isToken === false
    // if (getToken() && !isToken) {
    //   config.headers['Authorization'] = 'Bearer ' + getToken() // 让每个请求携带自定义token 请根据实际情况自行修改
    // }
    // get请求映射params参数
    if (config.method === 'get' && config.params) {
      let url = config.url + '?';
      for (const propName of Object.keys(config.params)) {
        const value = config.params[propName];
        var part = encodeURIComponent(propName) + "=";
        if (value !== null && typeof(value) !== "undefined") {
          if (typeof value === 'object') {
            for (const key of Object.keys(value)) {
              let params = propName + '[' + key + ']';
              var subPart = encodeURIComponent(params) + "=";
              url += subPart + encodeURIComponent(value[key]) + "&";
            }
          } else {
            url += part + encodeURIComponent(value) + "&";
          }
        }
      }
      url = url.slice(0, -1);
      config.params = {};
      config.url = url;
    }
    return config
  }, error => {
      Promise.reject(error)
  })

  // 响应拦截器
  service.interceptors.response.use(res => {
      console.log('---响应拦截器---',res)
        if (res.data.code === 0 && (res.data.msg === '未登录' || res.data.msg === 'NOTLOGIN')) {//返回登录页面
          window.top.location.href = '/front/page/login.html'
        } else {
        return res.data
      }
    },
    error => {
      let { message } = error;
      if (message == "Network Error") {
        message = "后端接口连接异常";
      }
      else if (message.includes("timeout")) {
        message = "系统接口请求超时";
      }
      else if (message.includes("Request failed with status code")) {
        message = "系统接口" + message.substr(message.length - 3) + "异常";
      }
      window.vant.Notify({
        message: message,
        type: 'warning',
        duration: 5 * 1000
      })
      //window.top.location.href = '/front/page/no-wify.html'
      return Promise.reject(error)
    }
// // 1. 获取状态码和消息
//           const code = res.data.code;
//           const msg = res.data.msg || '';
//
//           // 2. 判断是否需要跳转登录
//           // 使用 code == 0 兼容字符串"0"和数字0
//           // 只要 msg 是 'NOTLOGIN' 或 '未登录' 其中之一就跳转
//           if (code == 0 && (msg === 'NOTLOGIN' || msg === '未登录')) {
//
//               // 3. 执行跳转
//               // 优先尝试顶层窗口跳转，如果被禁用则使用当前窗口
//               if (window.top) {
//                   window.top.location.href = '/front/page/login.html';
//               } else {
//                   window.location.href = '/front/page/login.html';
//               }
//
//               // 返回一个未决的 Promise，中断后续业务逻辑，防止页面报错
//               return Promise.reject('请先登录');
//           }
//
//           // 3. 其他情况正常返回数据
//           return res.data;

  // }, error => {
  //       // // 处理 HTTP 错误 (如 401, 500)
  //       // console.log('---响应拦截器报错---', error);
  //       // return Promise.reject(error);
  //     // 处理网络错误等
  //     console.log('err' + error)
  //     let { message } = error;
  //     if (message == "Network Error") {
  //         message = "后端接口连接异常";
  //     } else if (message.includes("timeout")) {
  //         message = "系统接口请求超时";
  //     } else if (message.includes("Request failed with status code")) {
  //         message = "系统接口" + message.substr(message.length - 3) + "异常";
  //     }
  //
  //     // 如果你有引入 vant 组件库，可以在这里提示
  //     if (window.vant) {
  //         window.vant.Notify({
  //             message: message,
  //             type: 'warning',
  //             duration: 5 * 1000
  //         })
  //     }
  //     return Promise.reject(error)
  //     }
  )
  win.$axios = service
})(window);
