package Server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerSendFile {

    public static void send (Path path, Channel channel, ChannelFutureListener channelFutureListener) throws IOException {

        final char symbolToSend = '!';

        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path)); // при отправке файлы мы создадим файл region. Указываем путь к файлу, откуда начать передавать файл (0 - с начала), укзываем длину куса (размер файла)

        ByteBuf buf = null; // создаём байт буффер (размером 1 байт)
        buf = ByteBufAllocator.DEFAULT.directBuffer(1); // записываем сигнальный файл
        buf.writeByte((byte) symbolToSend);
        channel.write(buf); // посылаем в канал

        byte[] fileTitleInBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8); // преобразуем имя файла к String, String преобразуем в байты. Получили байты имени файла
        buf = ByteBufAllocator.DEFAULT.directBuffer(4); // создаём буфер на 4 байта
        buf.writeInt(fileTitleInBytes.length); //кладём в буфер число INT равное длине имени файла
        channel.write(buf); // посылаем полученный int в сеть

        buf = ByteBufAllocator.DEFAULT.directBuffer(fileTitleInBytes.length); //по длине имени файла выделяем место в буфере соответственного размера
        buf.writeBytes(fileTitleInBytes); // в буфер кладем все символы (массив байтов) имени файла
        channel.write(buf); // отправляем в сеть

        buf = ByteBufAllocator.DEFAULT.directBuffer(8); // выделяем в буфере 8 байтов (то есть 1 лонг) под размер нашего отправляемого файла
        buf.writeLong(Files.size(path)); // записываем в буфер размер файла
        channel.writeAndFlush(buf);// отправляем в сеть

        ChannelFuture sendOperation = channel.writeAndFlush(region); // отправляем файл в сеть в виде потока байтов

        if (channelFutureListener != null) {
            sendOperation.addListener(channelFutureListener); // результат ожидания на отправку файла // channelFutureListener прописывается в классе Client

        }
    }
}
