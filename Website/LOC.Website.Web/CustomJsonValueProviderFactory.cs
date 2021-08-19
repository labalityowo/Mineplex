namespace LOC.Website.Web
{
    using System;
    using System.Collections;
    using System.Collections.Generic;
    using System.Globalization;
    using System.IO;
    using System.Web.Mvc;
    using System.Web.Script.Serialization;

    public class CustomJsonValueProviderFactory : ValueProviderFactory
    {
        public override IValueProvider GetValueProvider(ControllerContext controllerContext)
        {
            if (controllerContext == null)
            {
                throw new ArgumentNullException("controllerContext");
            }

            var jsonData = GetDeserializedObject(controllerContext);
            if (jsonData == null)
            {
                return null;
            }

            var backingStore = new Dictionary<string, object>(StringComparer.OrdinalIgnoreCase);
            AddToBackingStore(backingStore, String.Empty, jsonData);
            return new DictionaryValueProvider<object>(backingStore, CultureInfo.CurrentCulture);
        }

        private static void AddToBackingStore(Dictionary<string, object> backingStore, string prefix, object value)
        {
            var d = value as IDictionary<string, object>;
            if (d != null)
            {
                foreach (var entry in d)
                {
                    AddToBackingStore(backingStore, MakePropertyKey(prefix, entry.Key), entry.Value);
                }

                return;
            }

            var l = value as IList;
            if (l != null)
            {
                for (var i = 0; i < l.Count; i++)
                {
                    AddToBackingStore(backingStore, MakeArrayKey(prefix, i), l[i]);
                }

                return;
            }

            // primitive
            backingStore[prefix] = value;
        }

        private static object GetDeserializedObject(ControllerContext controllerContext)
        {
            if (!controllerContext.HttpContext.Request.ContentType.StartsWith("application/json", StringComparison.OrdinalIgnoreCase))
            {
                // not JSON request
                return null;
            }

            var reader = new StreamReader(controllerContext.HttpContext.Request.InputStream);
            var bodyText = reader.ReadToEnd();
            if (String.IsNullOrEmpty(bodyText))
            {
                // no JSON data
                return null;
            }

            var serializer = new JavaScriptSerializer { MaxJsonLength = int.MaxValue };
            var jsonData = serializer.DeserializeObject(bodyText);
            return jsonData;
        }

        private static string MakeArrayKey(string prefix, int index)
        {
            return string.Format("{0}[{1}]", prefix, index.ToString(CultureInfo.InvariantCulture));
        }

        private static string MakePropertyKey(string prefix, string propertyName)
        {
            return String.IsNullOrEmpty(prefix) ? propertyName : string.Format("{0}.{1}", prefix, propertyName);
        }
    }
}