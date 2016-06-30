'''
Unit tests for hypy.
'''

import unittest
import os

# assumes hybridpy is on your PYTHONPATH
import hybridpy.hypy as hypy


def get_script_dir():
    '''get the dir path this script'''
    return os.path.dirname(os.path.realpath(__file__))

class TestHypy(unittest.TestCase):
    'Unit tests for hypy'

    def test_simple(self):
        '''run the simple example from the hyst readme'''
        
        model = get_script_dir() + "/../../../examples/toy/toy.xml"
        out_image = "toy_output.png"
        tool = "pysim" # pysim is the built-in simulator; try flowstar or spaceex

        e = hypy.Engine()
        e.set_model(model) # sets input model path
        e.set_tool(tool) # sets tool name to use
        #e.set_print_terminal_output(True) # print output to terminal? 
        #e.set_save_model_path('test_out.py') # save converted model?
        #e.set_output_image(out_image) # sets output image path
        #e.set_tool_params(["-tp", "jumps=2"]) # sets parameters for hyst conversion

        code = e.run(make_image=False)
        
        self.assertEqual(code, hypy.RUN_CODES.SUCCESS)

if __name__ == '__main__':
    unittest.main()
