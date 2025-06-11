package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.DiagnosisQuestion;
import soboro.soboro_web.domain.enums.DiagnosisType;
import soboro.soboro_web.repository.DiagnosisQuestionRepository;

import java.util.*;

@RestController
@RequestMapping("/api/admin/diagnosis-seed")
@RequiredArgsConstructor
public class DiagnosisSeedController {

    private final DiagnosisQuestionRepository questionRepository;

    //질문 저장 용도
    @PostMapping("/all")
    public Mono<String> seedAll() {
        Map<DiagnosisType, List<String>> questionMap = Map.of(
                DiagnosisType.DEPRESSION, List.of(
                        "기분이 가라앉거나 우울하거나 희망이 없다고 느꼈다.",
                        "평소 하던 일에 대한 흥미가 없어지거나 즐거움을 느끼지 못했다.",
                        "잠들기가 어렵거나 자주 깼다./혹은 너무 많이 잤다.",
                        "평소보다 식욕이 줄었다./혹은 평소보다 많이 먹었다.",
                        "다른 사람들이 눈치 챌 정도로 평소보다 말과 행동이 느려졌다./혹은 너무 안절부절 못해서 가만히 앉아있을 수 없다.",
                        "피곤하고 기운이 없었다.",
                        "내가 잘못했더나, 실패했다는 생각이 들었다./혹은 자신과 가족을 실망시켰다고 생각했다.",
                        "신문을 읽거나 TV를 보는 것과 같은 일상적인 일에도 집중할 수가 없었다.",
                        "차라리 죽는 것이 더 낫겠다고 생각했다. / 혹은 자해할 생각을 했다."
                ),
                DiagnosisType.ANXIETY, List.of(
                        "초조하거나 불안하거나 조마조마하게 느낀다.",
                        "걱정하는 것을 멈추거나 조절할 수가 없다.",
                        "여러 가지 것들에 대해 걱정을 너무 많이 한다.",
                        "편하게 있기가 어렵다.",
                        "너무 안절부절 못해서 가만히 있기가 힘들다.",
                        "쉽게 짜증이 나거나 쉽게 성을 내게 된다.",
                        "마치 끔찍한 일이 생길 것처럼 두렵게 느껴진다."
                ),
                DiagnosisType.STRESS, List.of(
                        "최근 1개월 동안, 예상치 못했던 일 때문에 당황했던 적이 얼마나 있었습니까?",
                        "최근 1개월 동안, 인생에서 중요한 일들을 조절할 수 없다는 느낌을 얼마나 경험하였습니까?",
                        "최근 1개월 동안, 신경이 예민해지고 스트레스를 받고 있다는 느낌을 얼마나 경험하였습니까?",
                        "최근 1개월 동안, 당신의 개인적 문제들을 다루는데 있어서 얼마나 자주 자신감을 느끼셨습니까?",
                        "최근 1개월 동안, 일상의 일들이 당신의 생각대로 진행되고 있다는 느낌을 얼마나 경험하였습니까?",
                        "최근 1개월 동안, 당신이 꼭 해야 하는 일을 처리할 수 없다고 생각한 적이 얼마나 있었습니까?",
                        "최근 1개월 동안, 일상생활의 짜증을 얼마나 자주 잘 다스릴 수 있었습니까?",
                        "최근 1개월 동안, 최상의 컨디션이라고 얼마나 자주 느끼셨습니까?",
                        "최근 1개월 동안, 당신이 통제할 수 없는 일 때문에 화가 난 경험이 얼마나 있었습니까?",
                        "최근 1개월 동안, 어려운 일들이 너무 많이 쌓여서 극복하지 못할 것 같은 느낌을 얼마나 자주 경험하였습니까?"
                )

                , DiagnosisType.EARLY_PSYCHOSIS, List.of(
                        "내가 이전에 즐겨 했던 일이 흥미가 없어진다.",
                        "지금 경험하는 일이 마치 전에도 똑같이 일어났던 것처럼 느껴질 때가 종종 있다(데자뷰).",
                        "나는 때때로 다른 사람들이 느끼지 못하는 냄새나 맛을 느낀다.",
                        "나는 쿵, 찰칵, 쉿, 짝짝, 딸랑딸랑 거리는 등의 특이한 소리를 종종 듣는다.",
                        "때로는 내가 경험한 상황이 실제인지 상상인지 헷갈릴 때가 있다.",
                        "내가 다른 사람을 쳐다보거나 거울 속의 내 자신을 볼 때, 내 눈 바로 앞에서 얼굴이 바뀌는 것을 본 적이 있다.",
                        "나는 사람을 처음 만날 때 극도로 불안해진다.",
                        "다른 사람들이 확실히 볼 수 없는 것들을 본 적이 있다.",
                        "내 생각들은 가끔 너무 강렬해져서 마치 실제로 내게 말하는 것처럼 느낄 때가 있다.",
                        "나는 때때로 광고나 상품 진열대 또는 내 주변에 배치된 것들에서 특별한 의미를 발견한다.",
                        "때때로 나는 내 아이디어나 생각을 통제하지 못한다고 느낄 때가 있다.",
                        "때때로 나는 평소에는 알아채지 못했을 희미한 소리에 갑자기 정신이 산만해 진다.",
                        "다른 사람들이 듣지 못하는 속삼임이나 말소리를 들은 적이 있다.",
                        "보이진 않지만, 어떤 사람이나 힘이 내 주위에 존재하는 느낌을 받은 적이 있다.",
                        "나는 내 몸의 일부가 어떤 면에서 달라졌거나, 이전과 다르게 움직이고 있다고 느낀다.",
                        "가끔 나에 대한 음모(모함)가 있다는 느낌이 든다.",
                        "사람들이 말을 길게 하면 말뜻을 정확히 이해하기 어렵다.",
                        "그럴 리가 없는데 가끔 어떤 사건이나 방송들이 나와 관련이 있는 것 같다."
                ),
                DiagnosisType.BIPOLAR, List.of(
                        "다음처럼 당신은 평소의 자신과는 달랐던 적이 과거(예전)에 있었습니까?",
                        "기분이 너무 좋거나 들떠서 다른 사람들이 평소의 당신 모습이 아니다라고 한 적이 있었다. 또는 너무 들떠서 문제가 생긴 적이 있었다.",
                        "지나치게 흥분하여 사람들에게 소리를 지르거나, 싸우거나 말다툼을 한 적이 있었다.",
                        "평소보다 더욱 자신감에 찬 적이 있었다.",
                        "평소보다 더욱 잠을 덜 잤거나, 또는 잠잘 필요를 느끼지 않은 적이 있었다.",
                        "평소보다 말이 더 많았거나 말이 매우 빨라졌던 적이 있었다.",
                        "생각이 머리 속에서 빠르게 돌아가는 것처럼 느꼈거나 마음을 차분하게 하지 못한 적이 있다.",
                        "주위에서 벌어지는 일로 쉽게 방해 받았기 때문에, 하던 일에 집중하기 어려웠거나 할 일을 계속하지 못한 적이 있었다.",
                        "평소보다 더욱 에너지가 넘쳤던 적이 있었다.",
                        "평소보다 더욱 활동적이었거나 더 많은 일을 하였던 적이 있었다.",
                        "평소보다 더욱 사교적이거나 적극적(외향적)이었던 적이 있었다.(하나의 예를 들면, 한밤중에 친구들에게 전화를 했다.)",
                        "평소보다 더욱 성행위에 관심이 간 적이 있었다.",
                        "평소의 당신과는 맞지 않는 행동을 했거나, 남들이 생각하기에 지나치거나 바보 같거나 또는 위험한 행동을 한 적이 있었다.",
                        "돈쓰는 문제로 자신이나 가족을 곤경에 빠뜨린 적이 있었다."
                ),
                DiagnosisType.INSOMNIA, List.of(
                        "잠들기 어려움",
                        "잠을 유지하기 어려움(자주 깸)",
                        "새벽에 너무 일찍 잠에서 깸",
                        "현재 수면패턴에 얼마나 만족하십니까?",
                        "불면증으로 인한 삶의 질 손상 정도가 다른 사람들에게 어떻게 보인다고 생각합니까?",
                        "현재 불면증에 관하여 얼마나 걱정하고 있습니까?",
                        "당신의 수면 문제가 일상 생활(예: 낮 동안 피곤함, 업무 또는 일상적 가사능력, 집중력, 기억력, 기분 등)을 어느 정도 방해한다고 생각합니까?"
                ),
                DiagnosisType.ALCOHOL, List.of(
                        "술은 얼마나 자주 마십니까?",
                        "평소 술을 마시는 날 몇잔 정도나 마십니까?",
                        "한 번 술을 마실 때 소주 1병 또는 맥주 4병 이상의 음주는 얼마나 자주 하십니까?",
                        "지난 1년간 술을 한 번 마시기 시작하면 멈출 수 없다는 것을 안 때가 얼마나 자주 있었습니까?",
                        "지난 1년간 평소 같으면 할 수 있었던 일을 음주 때문에 실패한 적이 얼마나 자주 있었습니까?",
                        "지난 1년간 술을 마신 다음날 아침에 일을 나가기 위해 다시 해장술이 필요했던 적이 얼마나 자주 있었습니까?",
                        "지난 1년간 음주 후에 죄책감이 들거나 후회를 한 적이 얼마나 자주 있었습니까?",
                        "지난 1년간 음주 때문에 전날 밤에 있었던 일이 기억나지 않았던 적이 얼마나 자주 있었습니까?",
                        "음주로 인해 자신이나 다른 사람이 다친 적이 있습니까?",
                        "친척이나 친구, 의사가 당신이 술 마시는 것을 걱정하거나 술 끊기를 권유한 적이 있습니까?"
                ),
                DiagnosisType.DEVICE_ADDICTION, List.of(
                        "[조절실패] 스마트폰 이용시간을 줄이려 할 때마다 실패한다.",
                        "[조절실패] 스마트폰 이용시간을 조절하는 것이 어렵다.",
                        "[조절실패] 적절한 스마트폰 이용시간을 지키는 것이 어렵다.",
                        "[현저성] 스마트폰이 옆에 있으면 다른일에 집중하기 어렵다.",
                        "[현저성] 스마트폰 생각이 머리에서 떠나지 않는다.",
                        "[현저성] 스마트폰을 이용하고 싶은 충동을 강하게 느낀다.",
                        "[문제적 결과] 스마트폰 이용 때문에 건강에 문제가 생긴 적이 있다.",
                        "[문제적 결과] 스마트폰 이용 때문에 가족과 심하게 다툰 적이 있다.",
                        "[문제적 결과] 스마트폰 이용 때문에 친구 혹은 동료, 사회적 관계에서 심한 갈등을 경험한 적이 있다.",
                        "[문제적 결과] 스마트폰 때문에 업무(학업 혹은 직업 등) 수행에 어려움이 있다."
                )

        );

        List<Mono<String>> operations = new ArrayList<>();

        for (var entry : questionMap.entrySet()) {
            DiagnosisType type = entry.getKey();
            List<String> questions = entry.getValue();

            Mono<String> op = questionRepository.findAllByTypeOrderByNumber(type)
                    .hasElements()
                    .flatMap(exists -> {
                        if (exists) {
                            return Mono.just("✅ " + type.name() + " 이미 있음 - 등록 생략");
                        }

                        List<DiagnosisQuestion> list = new ArrayList<>();
                        for (int i = 0; i < questions.size(); i++) {
                            DiagnosisQuestion q = new DiagnosisQuestion();
                            q.setType(type);
                            q.setNumber(i + 1);
                            q.setQuestionText(questions.get(i));
                            list.add(q);
                        }

                        return questionRepository.saveAll(list)
                                .then(Mono.just("✅ " + type.name() + " 등록 완료"));
                    });

            operations.add(op);
        }
        return Flux.merge(operations)
                .collectList()
                .map(results -> String.join("\n", results));
    }

}
