"""python3 -m unittest discover -s scripts"""

import importlib.util
import tempfile
import unittest
from pathlib import Path
from unittest import mock

spec = importlib.util.spec_from_file_location("play_metadata", Path(__file__).with_name("play-metadata.py"))
play_metadata = importlib.util.module_from_spec(spec)
spec.loader.exec_module(play_metadata)


class PlayLocale(unittest.TestCase):
    def test_a_mapped_tag_is_used_as_it_is(self):
        self.assertEqual(play_metadata.play_locale("pt-BR"), "pt-BR")

    def test_a_device_tag_falls_back_to_its_language(self):
        self.assertEqual(play_metadata.play_locale("en-US"), "en-US")
        self.assertEqual(play_metadata.play_locale("pl-PL"), "pl-PL")
        self.assertEqual(play_metadata.play_locale("de-DE"), "de-DE")

    def test_an_unshipped_language_has_no_play_locale(self):
        self.assertIsNone(play_metadata.play_locale("ja-JP"))


class CopyScreenshots(unittest.TestCase):
    def test_an_unknown_tag_is_reported_not_copied(self):
        with tempfile.TemporaryDirectory() as tmp:
            shots = Path(tmp) / "shots"
            shots.mkdir()
            for name in ("1_home-pl-PL.png", "1_home-ja-JP.png"):
                (shots / name).write_bytes(b"png")
            out = Path(tmp) / "out"
            with mock.patch.object(play_metadata, "SCREENSHOTS", shots):
                counts, unknown = play_metadata.copy_screenshots(out)
            self.assertEqual(counts, {"pl-PL": 1})
            self.assertEqual(unknown, ["1_home-ja-JP.png"])
            self.assertTrue((out / "pl-PL" / "images" / "phoneScreenshots" / "1_home.png").exists())


if __name__ == "__main__":
    unittest.main()
