namespace LOC.Core.Data
{
    using System;

    public interface IRestCallJsonWrapper
    {
        T MakeCall<T>(object data, Uri uri, RestCallType restCallType, double timeoutInSeconds = 100);
        void MakeCall(object data, Uri uri, RestCallType restCallType, double timeoutInSeconds = 100);
    }
}
