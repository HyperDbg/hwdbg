// Generated by CIRCT firtool-1.62.0
module BufferedTx(
  input        clock,
               reset,
  output       io_txd,
               io_channel_ready,
  input        io_channel_valid,
  input  [7:0] io_channel_bits
);

  wire       _buf_io_out_valid;
  wire [7:0] _buf_io_out_bits;
  wire       _tx_io_channel_ready;
  Tx tx (
    .clock            (clock),
    .reset            (reset),
    .io_txd           (io_txd),
    .io_channel_ready (_tx_io_channel_ready),
    .io_channel_valid (_buf_io_out_valid),
    .io_channel_bits  (_buf_io_out_bits)
  );
  Buffer buf_0 (
    .clock        (clock),
    .reset        (reset),
    .io_in_ready  (io_channel_ready),
    .io_in_valid  (io_channel_valid),
    .io_in_bits   (io_channel_bits),
    .io_out_ready (_tx_io_channel_ready),
    .io_out_valid (_buf_io_out_valid),
    .io_out_bits  (_buf_io_out_bits)
  );
endmodule

