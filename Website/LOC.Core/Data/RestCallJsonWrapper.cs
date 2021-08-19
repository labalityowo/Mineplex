namespace LOC.Core.Data
{
    using System;
    using System.IO;
    using System.Net;
    using System.Web;
    using Newtonsoft.Json;

    public class RestCallJsonWrapper : IRestCallJsonWrapper
    {
        public T MakeCall<T>(object data, Uri uri, RestCallType restCallType, double timeoutInSeconds)
        {
            var response = InitiateRequest(uri, restCallType, data, timeoutInSeconds);
            T result;
            using (var streamReader = new StreamReader(response.GetResponseStream()))
            {
                var responseText = streamReader.ReadToEnd();
                result = JsonConvert.DeserializeObject<T>(responseText);
            }

            return result;
        }

        public void MakeCall(object data, Uri uri, RestCallType restCallType, double timeoutInSeconds)
        {
            var response = InitiateRequest(uri, restCallType, data);
            if (response.StatusCode
                != HttpStatusCode.OK)
            {
                // TODO: need handling code here
                throw new HttpException((int)response.StatusCode, "Received a not OK response from the server.");
            }
        }

        private static HttpWebResponse InitiateRequest(Uri uri, RestCallType restCallType, object data, double timeoutInSeconds = 100)
        {
            var request = (HttpWebRequest)WebRequest.Create(uri);
            request.Timeout = (int)TimeSpan.FromSeconds(timeoutInSeconds).TotalMilliseconds;
            request.ContentType = "application/json";
            switch (restCallType)
            {
                case RestCallType.Get:
                    request.Method = "GET";
                    break;
                case RestCallType.Post:
                    request.Method = "POST";
                    break;
                default:
                    throw new HttpException("REST call type not defined.");
            }

            if (data != null)
            {
                using (var streamWriter = new StreamWriter(request.GetRequestStream()))
                {
                    var json = JsonConvert.SerializeObject(data);
                    streamWriter.Write(json);
                }
            }

            return (HttpWebResponse)request.GetResponse();
        }
    }
}
