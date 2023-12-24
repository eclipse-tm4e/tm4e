<%@ Page Language="VB" AutoEventWireup="false" CodeBehind="Default.aspx.vb" Inherits="WebApplication.Default" %>

<!DOCTYPE html>

<html lang="en">
<head runat="server">
  <meta charset="utf-8" />
  <title>VB.NET + ASP.NET Example</title>
</head>
<body>
  <form id="form1" runat="server">
    <div>
      <% ' Server-side VB.NET code %>
      <%
        Dim greeting As String = "Hello, ASP.NET!"
        Response.Write(greeting)
      %>

      <br />

      <% ' Server-side control %>
      <asp:Label ID="lblMessage" runat="server" Text=""></asp:Label>

      <br />

      <% ' Code-behind interaction %>
      <%
        Dim name As String = "Alice"
        lblMessage.Text = $"Welcome, {name}!"
      %>

      <br />

      <% ' VB.NET Function %>
      <%
        Function AddNumbers(a As Integer, b As Integer) As Integer
          Return a + b
        End Function

        Dim result As Integer = AddNumbers(5, 7)
        Response.Write($"Sum: {result}")
      %>
    </div>
  </form>
</body>
</html>