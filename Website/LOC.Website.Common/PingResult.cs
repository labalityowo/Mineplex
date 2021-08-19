namespace LOC.Website.Common
{
    namespace LobbyProxy
    {
        using System;
        using System.Net.Sockets;
        using System.Text;

        public class PingResult
        {
            public string MessageOfTheDay { get; set; }

            public int Players { get; set; }

            public int MaxPlayers { get; set; }

            public bool Success { get; set; }

            public static PingResult Ping(string address, int port)
            {
                var pr = new PingResult();

                try
                {
                    using (var socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp))
                    {
                        socket.Connect(address, port);
                        socket.ReceiveTimeout = 500;
                        socket.SendTimeout = 500;

                        socket.Send(new byte[] {0xFE, 0x01});
                        var b = new byte[1024];
                        int len = socket.Receive(b);

                        if (len < 3)
                        {
                            pr.Success = false;
                            return pr;
                        }

                        var result = Encoding.BigEndianUnicode.GetString(b, 3, len - 3);
                        var parts = result.Split('\0');

                        if (parts.Length != 6)
                        {
                            pr.Success = false;
                            return pr;
                        }

                        int players;
                        int max;

                        pr.MessageOfTheDay = parts[3];

                        if (!int.TryParse(parts[4], out players))
                        {
                            pr.Success = false;
                            return pr;
                        }

                        if (!int.TryParse(parts[5], out max))
                        {
                            pr.Success = false;
                            return pr;
                        }

                        pr.Players = players;
                        pr.MaxPlayers = max;

                        pr.Success = true;

                        socket.Disconnect(false);
                        socket.Close();
                        socket.Dispose();
                    }
                }
                catch (Exception)
                {
                    // Gulp
                }

                return pr;
            }
        }
    }

}
