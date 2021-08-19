namespace LOC.Core
{
    using System.IO;
    using System.Web.Mvc;
    using Newtonsoft.Json;

    public class JsonModelBinder : DefaultModelBinder
    {
        public override object BindModel(ControllerContext controllerContext, ModelBindingContext bindingContext)
        {
            if (!IsJsonRequest(controllerContext))
                return base.BindModel(controllerContext, bindingContext);
            var request = controllerContext.HttpContext.Request;
            var stream = request.InputStream;
            if (stream.Length == 0)
                return null;
            stream.Position = 0;
            var jsonStringData = new StreamReader(stream).ReadToEnd();
            return JsonConvert.DeserializeObject(jsonStringData, bindingContext.ModelType);
        }

        private static bool IsJsonRequest(ControllerContext controllerContext)
        {
            var contentType = controllerContext.HttpContext.Request.ContentType;
            return contentType.Contains("application/json");
        }
    }
}
