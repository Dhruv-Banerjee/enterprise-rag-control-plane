import { createServer } from "node:http";
import { readFile } from "node:fs/promises";
import { join } from "node:path";
import { fileURLToPath } from "node:url";

const root = fileURLToPath(new URL(".", import.meta.url));
const port = 4173;

const server = createServer(async (request, response) => {
  const requestPath = request.url === "/" ? "/index.html" : request.url;
  const safePath = requestPath.replaceAll("..", "");
  const filePath = join(root, safePath);

  try {
    const content = await readFile(filePath);
    const contentType = filePath.endsWith(".html") ? "text/html; charset=utf-8" : "text/plain; charset=utf-8";
    response.writeHead(200, { "Content-Type": contentType });
    response.end(content);
  } catch {
    response.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
    response.end("Not found");
  }
});

server.listen(port, "127.0.0.1");
