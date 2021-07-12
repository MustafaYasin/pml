import torch
import torchvision
from torch.utils.mobile_optimizer import optimize_for_mobile
from models.experimental import attempt_load
from utils.torch_utils import select_device


model = torch.load('best_jannis_v1.torchscript.pt', map_location=torch.device('cpu'))
print(model)
model.eval()



scripted_module = torch.jit.script(model)
# Export full jit version model (not compatible mobile interpreter), leave it here for comparison
#scripted_module.save("best_scripted.pt")
# Export mobile interpreter version model (compatible with mobile interpreter)
optimized_scripted_module = optimize_for_mobile(scripted_module)
optimized_scripted_module._save_for_lite_interpreter("best_jannis_v1_scripted.ptl")





# python export.py --weights best_jannis_v1.pt --img 640 --batch 1
# python torch-to-mobile.p