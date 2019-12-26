#ifndef AUDIO_PROCESS_ENCODER_ADAPTER_H
#define AUDIO_PROCESS_ENCODER_ADAPTER_H

#include "../../liblivecore/audio_encoder/audio_encoder_adapter.h"
#include "../../video_consumer/libmusic_merger/music_merger.h"
#include "../../video_consumer/libvideo_consumer/live_common_packet_pool.h"
#include "../../audio_effect/libaudio_effect/audio_effect/audio_effect.h"

class AudioProcessEncoderAdapter: public AudioEncoderAdapter {
public:
	AudioProcessEncoderAdapter();
    virtual ~AudioProcessEncoderAdapter();

    void init(LivePacketPool* pcmPacketPool, int audioSampleRate, int audioChannels,
    		int audioBitRate, const char* audio_codec_name, AudioEffect *audioEffect);

	void setAudioEffect(AudioEffect *audioEffect);

    virtual void destroy();
protected:
	LiveCommonPacketPool* accompanyPacketPool;
	MusicMerger* musicMerger;

    virtual void discardAudioPacket();
    virtual int processAudio();

};
#endif // AUDIO_PROCESS_ENCODER_ADAPTER_H
